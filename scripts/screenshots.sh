#!/bin/bash
# =====================================================================
# Di2048 — инструмент для скриншотов и промо-видео
# =====================================================================
#
# Возможности:
#   1. Скриншоты через adb (с автоматической обрезкой в 9:16 по центру)
#   2. Обрезка существующих скриншотов в 9:16 (для файлов с неправильным
#      соотношением сторон)
#   3. Запись промо-видео через adb shell screenrecord + обрезка в 9:16
#      через ffmpeg
#
# Использование:
#   ./scripts/screenshots.sh              # интерактивное меню
#   ./scripts/screenshots.sh crop         # обрезать все PNG в Materials/screens в 9:16
#   ./scripts/screenshots.sh screenshot   # сделать один скриншот
#   ./scripts/screenshots.sh video        # записать промо-видео
#
# Требования:
#   - adb (Android Debug Bridge) — для скриншотов и видео с устройства
#   - Python 3 + Pillow — для обрезки PNG
#   - ffmpeg — для обрезки видео
#
# Соотношение сторон 9:16 = 0.5625 (стандарт для store-скриншотов и промо)
# Целевое разрешение по умолчанию: 1080x1920 (PNG), 1080x1920 (видео)
# =====================================================================

set -e

# --- Цвета ---
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# --- Константы ---
SCREENS_DIR="Materials/screens"
VIDEOS_DIR="Materials/videos"
TEMP_DEVICE_PATH="/sdcard/di2048_capture"
TARGET_RATIO="9:16"           # соотношение сторон для store
TARGET_RATIO_FLOAT=0.5625     # 9/16
TARGET_WIDTH=1080
TARGET_HEIGHT=1920

# --- Проверка зависимостей ---
# $1 = список требуемых инструментов через пробел: "adb", "ffmpeg", "pil"
check_deps() {
    local required="$1"
    local missing=()
    for tool in $required; do
        case "$tool" in
            adb)
                command -v adb >/dev/null 2>&1 || missing+=("adb")
                ;;
            ffmpeg)
                command -v ffmpeg >/dev/null 2>&1 || missing+=("ffmpeg")
                ;;
            pil)
                python3 -c "from PIL import Image" 2>/dev/null || missing+=("python3-pil (Pillow)")
                ;;
            python3)
                command -v python3 >/dev/null 2>&1 || missing+=("python3")
                ;;
        esac
    done
    if [[ "${#missing[@]}" -gt 0 ]]; then
        echo -e "${RED}Отсутствуют зависимости: ${missing[*]}${NC}"
        echo "Установите: sudo apt install adb python3-pil ffmpeg"
        exit 1
    fi
}

# --- Проверка подключения устройства ---
check_device() {
    if ! adb devices 2>/dev/null | grep -q "device$"; then
        echo -e "${RED}Устройство не подключено.${NC}"
        echo "Подключите устройство через USB и включите отладку по USB."
        exit 1
    fi
    local DEVICE
    DEVICE=$(adb devices | grep "device$" | head -1 | cut -f1)
    echo -e "${GREEN}Устройство: $DEVICE${NC}"
}

# --- Создание директорий ---
ensure_dirs() {
    mkdir -p "$SCREENS_DIR" "$VIDEOS_DIR"
}

# =====================================================================
# ОБРЕЗКА PNG В 9:16 (по центру)
# =====================================================================
# Использует Pillow. Обрезает по центру так, чтобы получилось ровно 9:16.
# Если исходное изображение шире нужного — обрезает слева/справа.
# Если выше нужного — обрезает сверху/снизу.
# Целевой размер: 1080x1920 (или меньше, если исходник меньше).
# =====================================================================
crop_png_to_9_16() {
    local src="$1"
    local dst="$2"

    python3 - "$src" "$dst" "$TARGET_RATIO_FLOAT" "$TARGET_WIDTH" "$TARGET_HEIGHT" <<'PYEOF'
import sys
from PIL import Image

src = sys.argv[1]
dst = sys.argv[2]
target_ratio = float(sys.argv[3])   # 0.5625
target_w = int(sys.argv[4])         # 1080
target_h = int(sys.argv[5])         # 1920

img = Image.open(src)
w, h = img.size
current_ratio = w / h

if current_ratio > target_ratio:
    # Изображение слишком широкое — обрезаем по ширине (слева/справа)
    new_w = int(h * target_ratio)
    left = (w - new_w) // 2
    img = img.crop((left, 0, left + new_w, h))
else:
    # Изображение слишком высокое — обрезаем по высоте (сверху/снизу)
    new_h = int(w / target_ratio)
    top = (h - new_h) // 2
    img = img.crop((0, top, w, top + new_h))

# Масштабируем до целевого размера (1080x1920), если исходник больше
if img.size[0] >= target_w:
    img = img.resize((target_w, target_h), Image.LANCZOS)
else:
    # Если исходник меньше — сохраняем как есть, но с правильным соотношением
    pass

img.save(dst, "PNG")
print(f"{w}x{h} -> {img.size[0]}x{img.size[1]}")
PYEOF
}

# =====================================================================
# КОМАНДА: crop — обрезать все PNG в Materials/screens в 9:16
# =====================================================================
cmd_crop_all() {
    echo -e "${BLUE}=== Обрезка скриншотов в 9:16 ===${NC}"
    check_deps "python3 pil"
    ensure_dirs

    local count=0
    local skipped=0
    local files=()
    # Собираем PNG-файлы
    while IFS= read -r f; do
        files+=("$f")
    done < <(find "$SCREENS_DIR" -maxdepth 1 -name "*.png" -type f | sort)

    if [[ ${#files[@]} -eq 0 ]]; then
        echo -e "${YELLOW}В $SCREENS_DIR нет PNG-файлов.${NC}"
        return
    fi

    for f in "${files[@]}"; do
        local basename
        basename=$(basename "$f")
        # Проверяем соотношение сторон
        local dims
        dims=$(python3 -c "from PIL import Image; img=Image.open('$f'); print(f'{img.size[0]} {img.size[1]} {img.size[0]/img.size[1]:.4f}')")
        local w h ratio
        w=$(echo "$dims" | awk '{print $1}')
        h=$(echo "$dims" | awk '{print $2}')
        ratio=$(echo "$dims" | awk '{print $3}')

        # 9:16 = 0.5625. Допуск ±0.005 (на случай округления пикселей).
        # Используем python для сравнения float (bc может отсутствовать).
        local is_9_16
        is_9_16=$(python3 -c "print(1 if abs($ratio - 0.5625) < 0.005 else 0)")

        if [[ "$is_9_16" == "1" ]]; then
            echo -e "  ${GREEN}✓${NC} $basename  ${w}x${h} (ratio $ratio — уже 9:16)"
            skipped=$((skipped + 1))
            continue
        fi

        echo -ne "  ${YELLOW}→${NC} $basename  ${w}x${h} (ratio $ratio) — обрезка... "
        local tmp="${f}.cropped.png"
        if crop_png_to_9_16 "$f" "$tmp"; then
            mv "$tmp" "$f"
            echo -e "${GREEN}готово${NC}"
            count=$((count + 1))
        else
            rm -f "$tmp"
            echo -e "${RED}ошибка${NC}"
        fi
    done

    echo ""
    echo -e "${GREEN}Обрезано: $count, пропущено (уже 9:16): $skipped${NC}"
}

# =====================================================================
# КОМАНДА: screenshot — сделать один скриншот с обрезкой
# =====================================================================
cmd_screenshot() {
    echo -e "${BLUE}=== Снимок экрана ===${NC}"
    check_deps "adb python3 pil"
    check_device
    ensure_dirs

    local name="${1:-manual}"
    local timestamp
    timestamp=$(date +%Y%m%d_%H%M%S)
    local filename="scr_${name}_${timestamp}.png"
    local filepath="$SCREENS_DIR/$filename"

    echo -e "${YELLOW}Снимок экрана с устройства...${NC}"
    adb shell screencap -p "$TEMP_DEVICE_PATH.png"
    adb pull "$TEMP_DEVICE_PATH.png" "$filepath"
    adb shell rm "$TEMP_DEVICE_PATH.png"

    echo -e "${YELLOW}Обрезка в 9:16...${NC}"
    local tmp="${filepath}.cropped.png"
    if crop_png_to_9_16 "$filepath" "$tmp"; then
        mv "$tmp" "$filepath"
        local dims
        dims=$(python3 -c "from PIL import Image; img=Image.open('$filepath'); print(f'{img.size[0]}x{img.size[1]}')")
        echo -e "${GREEN}✓ Сохранено: $filepath (${dims})${NC}"
    else
        rm -f "$tmp"
        echo -e "${RED}Ошибка обрезки. Исходный файл: $filepath${NC}"
    fi
}

# =====================================================================
# КОМАНДА: video — запись промо-видео с экрана устройства
# =====================================================================
# Использует adb shell screenrecord (встроен в Android 4.4+).
# Ограничения screenrecord:
#   - Максимум 3 минуты на файл (можно продлить параметром --time-limit)
#   - Максимум 1080p
#   - Не записывает аудио
# После записи ffmpeg обрезает видео в 9:16 по центру.
# =====================================================================
cmd_video() {
    echo -e "${BLUE}=== Запись промо-видео ===${NC}"
    check_deps "adb python3 pil ffmpeg"
    check_device
    ensure_dirs

    local name="${1:-promo}"
    local duration="${2:-30}"
    local timestamp
    timestamp=$(date +%Y%m%d_%H%M%S)
    local filename="video_${name}_${timestamp}.mp4"
    local filepath="$VIDEOS_DIR/$filename"
    local raw_path="${VIDEOS_DIR}/${filename}.raw.mp4"

    echo -e "${YELLOW}Запись ${duration}s видео с экрана устройства...${NC}"
    echo -e "${BLUE}Подготовьте экран (откройте нужный игровой сценарий).${NC}"
    read -p "Нажмите Enter для начала записи (${duration}s)..."

    # screenrecord пишет на устройство; затем pull на хост
    adb shell screenrecord --time-limit "$duration" --bit-rate 8000000 "$TEMP_DEVICE_PATH.mp4"
    echo -e "${YELLOW}Загрузка видео с устройства...${NC}"
    adb pull "$TEMP_DEVICE_PATH.mp4" "$raw_path"
    adb shell rm "$TEMP_DEVICE_PATH.mp4"

    # Получаем размеры исходного видео
    local dims
    dims=$(ffprobe -v error -select_streams v:0 -show_entries stream=width,height -of csv=p=0 "$raw_path")
    local vw vh
    vw=$(echo "$dims" | cut -d',' -f1)
    vh=$(echo "$dims" | cut -d',' -f2)
    echo -e "${YELLOW}Исходное видео: ${vw}x${vh}${NC}"

    # Обрезка в 9:16 через ffmpeg crop filter (по центру)
    # crop=W:H:X:Y — W и H целевые, X/Y — смещение
    # Для 9:16 из видео vw x vh:
    #   если vw/vh > 9/16 (слишком широкое): new_w = vh*9/16, new_h = vh, x=(vw-new_w)/2, y=0
    #   если vw/vh < 9/16 (слишком узкое/высокое): new_w = vw, new_h = vw*16/9, x=0, y=(vh-new_h)/2
    local new_w new_h crop_x crop_y
    python3 - "$vw" "$vh" <<'PYEOF' > /tmp/di2048_crop.txt
import sys
vw = int(sys.argv[1])
vh = int(sys.argv[2])
target = 9.0 / 16.0
current = vw / vh
if current > target:
    # слишком широкое — обрезаем по ширине
    new_w = int(vh * target)
    # сделаем new_w чётным (требование ffmpeg)
    if new_w % 2 == 1: new_w -= 1
    new_h = vh
    x = (vw - new_w) // 2
    y = 0
else:
    # слишком высокое — обрезаем по высоте
    new_w = vw
    new_h = int(vw / target)
    if new_h % 2 == 1: new_h -= 1
    x = 0
    y = (vh - new_h) // 2
print(f"{new_w}:{new_h}:{x}:{y}")
PYEOF
    local crop_filter
    crop_filter=$(cat /tmp/di2048_crop.txt)
    rm -f /tmp/di2048_crop.txt
    echo -e "${YELLOW}Обрезка ffmpeg crop=$crop_filter...${NC}"

    # Масштабируем до 1080x1920 с сохранением пропорций, затем кодируем
    ffmpeg -y -i "$raw_path" \
        -vf "crop=$crop_filter,scale=1080:1920,setsar=1" \
        -c:v libx264 -preset medium -crf 23 \
        -movflags +faststart \
        "$filepath" 2>&1 | tail -5

    rm -f "$raw_path"

    if [[ -f "$filepath" ]]; then
        local final_dims
        final_dims=$(ffprobe -v error -select_streams v:0 -show_entries stream=width,height -of csv=p=0 "$filepath")
        echo -e "${GREEN}✓ Сохранено: $filepath (${final_dims})${NC}"
    else
        echo -e "${RED}Ошибка кодирования видео${NC}"
    fi
}

# =====================================================================
# ИНТЕРАКТИВНОЕ МЕНЮ (по умолчанию)
# =====================================================================
cmd_interactive() {
    echo -e "${BLUE}======================================${NC}"
    echo -e "${BLUE}  Di2048 — инструмент скриншотов/видео${NC}"
    echo -e "${BLUE}======================================${NC}"
    echo ""
    echo "Команды:"
    echo "  1  — сделать скриншот (с обрезкой 9:16)"
    echo "  2  — обрезать ВСЕ существующие скриншоты в 9:16"
    echo "  3  — записать промо-видео (${duration}s по умолчанию)"
    echo "  4  — записать промо-видео с указанием длительности"
    echo "  q  — выход"
    echo ""

    while true; do
        read -p "Действие: " -n 1 -r ACTION
        echo ""
        case $ACTION in
            1)
                read -p "Имя скриншота (например game_over): " NAME
                cmd_screenshot "${NAME:-manual}"
                ;;
            2)
                cmd_crop_all
                ;;
            3)
                cmd_video "promo" "30"
                ;;
            4)
                read -p "Длительность в секундах (макс 180): " DUR
                cmd_video "promo" "${DUR:-30}"
                ;;
            q|Q)
                exit 0
                ;;
            *)
                echo -e "${RED}Неверная команда${NC}"
                ;;
        esac
        echo ""
    done
}

# =====================================================================
# MAIN
# =====================================================================
case "${1:-}" in
    crop)
        cmd_crop_all
        ;;
    screenshot)
        cmd_screenshot "${2:-manual}"
        ;;
    video)
        cmd_video "${2:-promo}" "${3:-30}"
        ;;
    ""|menu|interactive)
        cmd_interactive
        ;;
    *)
        echo "Использование: $0 [crop|screenshot [name]|video [name] [duration]|menu]"
        echo ""
        echo "Команды:"
        echo "  crop                    — обрезать все PNG в Materials/screens в 9:16"
        echo "  screenshot [name]       — сделать скриншот с обрезкой"
        echo "  video [name] [duration] — записать промо-видео с обрезкой"
        echo "  menu                    — интерактивное меню (по умолчанию)"
        exit 1
        ;;
esac
