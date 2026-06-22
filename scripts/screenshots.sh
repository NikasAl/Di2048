#!/bin/bash
# Скрипт для создания скриншотов 9:16 для RuStore
# Использование: ./scripts/screenshots.sh

set -e

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Пути
SCREENS_DIR="Materials/screens"
TEMP_SCREENSHOT="/sdcard/screenshot.png"

# Разрешение 9:16 (стандартное для телефонов)
WIDTH=1080
HEIGHT=1920

# Счётчик скриншотов
SCREENSHOT_NUM=1

echo -e "${BLUE}======================================${NC}"
echo -e "${BLUE}  Скриншоты для RuStore (9:16)${NC}"
echo -e "${BLUE}======================================${NC}"
echo ""

# Проверка подключения устройства
echo -e "${YELLOW}Проверка подключения устройства...${NC}"
if ! adb devices | grep -q "device$"; then
    echo -e "${RED}Ошибка: Устройство не подключено или не найдено.${NC}"
    echo "Подключите устройство через USB и включите отладку по USB."
    exit 1
fi

DEVICE=$(adb devices | grep "device$" | head -1 | cut -f1)
echo -e "${GREEN}Устройство найдено: $DEVICE${NC}"
echo ""

# Получение текущего разрешения
echo -e "${YELLOW}Текущее разрешение экрана:${NC}"
CURRENT_RES=$(adb shell wm size | grep -oP '\d+x\d+')
echo "  $CURRENT_RES"
echo ""

# Предложение установить разрешение 9:16
echo -e "${YELLOW}Хотите установить разрешение ${WIDTH}x${HEIGHT} (9:16)?${NC}"
echo -e "${BLUE}Это нужно для корректных скриншотов в формате RuStore.${NC}"
echo ""
read -p "Установить разрешение 9:16? (y/n): " -n 1 -r SET_RES
echo ""

if [[ $SET_RES =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}Установка разрешения ${WIDTH}x${HEIGHT}...${NC}"
    adb shell wm size ${WIDTH}x${HEIGHT}
    
    # Проверка установленного разрешения (с небольшой задержкой для применения)
    sleep 1.0
    NEW_RES=$(adb shell wm size | grep -oP '\d+x\d+')
    if [[ "$NEW_RES" == "${WIDTH}x${HEIGHT}" ]]; then
        echo -e "${GREEN}Разрешение успешно установлено: $NEW_RES${NC}"
    else
        echo -e "${RED}Не удалось установить разрешение. Текущее: $NEW_RES${NC}"
        echo -e "${YELLOW}Возможно, требуется перезапуск приложения для применения.${NC}"
    fi
    echo ""
    
    # Сохраняем исходное разрешение для восстановления
    ORIGINAL_RES="$CURRENT_RES"
    trap "restore_resolution" EXIT
    
    restore_resolution() {
        echo ""
        echo -e "${YELLOW}Восстановление исходного разрешения ($ORIGINAL_RES)...${NC}"
        adb shell wm size $ORIGINAL_RES
        adb shell wm density reset
        echo -e "${GREEN}Готово!${NC}"
    }
fi

# Создание директории для скриншотов
if [[ ! -d "$SCREENS_DIR" ]]; then
    mkdir -p "$SCREENS_DIR"
    echo -e "${GREEN}Создана директория: $SCREENS_DIR${NC}"
fi

# Меню сценариев для скриншотов
echo -e "${BLUE}======================================${NC}"
echo -e "${BLUE}  Сценарии для скриншотов${NC}"
echo -e "${BLUE}======================================${NC}"
echo ""
echo "1. Главное меню / игровое поле 4×4"
echo "2. Поле 8×8 (масштаб и рекорд)"
echo "3. Экран Game Over (кнопки Продолжить/Отменить)"
echo "4. Кнопка UNDO (крупный план)"
echo "5. Настройки (выбор размера поля)"
echo "6. Другой экран (ручной режим)"
echo "0. Выход"
echo ""

# Основной цикл
while true; do
    echo -e "${YELLOW}--- Скриншот #$SCREENSHOT_NUM ---${NC}"
    echo ""
    echo "Доступные команды:"
    echo "  1-6  — выбрать сценарий и сделать скриншот"
    echo "  s    — сделать скриншот прямо сейчас"
    echo "  r    — восстановить исходное разрешение и выйти"
    echo "  q    — выйти (без восстановления разрешения)"
    echo ""
    
    read -p "Действие: " -n 1 -r ACTION
    echo ""
    
    case $ACTION in
        [1-6])
            case $ACTION in
                1) SCENARIO_NAME="main_menu_4x4" ;;
                2) SCENARIO_NAME="field_8x8" ;;
                3) SCENARIO_NAME="game_over" ;;
                4) SCENARIO_NAME="undo_button" ;;
                5) SCENARIO_NAME="settings" ;;
                6) SCENARIO_NAME="custom" ;;
            esac
            
            echo ""
            echo -e "${BLUE}Перейдите к экрану: $SCENARIO_NAME${NC}"
            read -p "Нажмите Enter, когда будете готовы..."
            
            echo -e "${YELLOW}Создание скриншота...${NC}"
            adb shell screencap -p "$TEMP_SCREENSHOT"
            
            FILENAME="scr_${SCREENSHOT_NUM}_${SCENARIO_NAME}.png"
            FILEPATH="$SCREENS_DIR/$FILENAME"
            
            adb pull "$TEMP_SCREENSHOT" "$FILEPATH"
            
            echo -e "${GREEN}✓ Сохранено: $FILEPATH${NC}"
            echo ""
            
            SCREENSHOT_NUM=$((SCREENSHOT_NUM + 1))
            ;;
        
        s|S)
            echo -e "${YELLOW}Создание скриншота...${NC}"
            adb shell screencap -p "$TEMP_SCREENSHOT"
            
            FILENAME="scr_${SCREENSHOT_NUM}_manual.png"
            FILEPATH="$SCREENS_DIR/$FILENAME"
            
            adb pull "$TEMP_SCREENSHOT" "$FILEPATH"
            
            echo -e "${GREEN}✓ Сохранено: $FILEPATH${NC}"
            echo ""
            
            SCREENSHOT_NUM=$((SCREENSHOT_NUM + 1))
            ;;
        
        r|R)
            echo -e "${YELLOW}Восстановление настроек и выход...${NC}"
            adb shell wm size reset
            adb shell wm density reset
            echo -e "${GREEN}Готово!${NC}"
            exit 0
            ;;
        
        q|Q)
            echo -e "${YELLOW}Выход (разрешение не восстановлено)${NC}"
            echo -e "${BLUE}Для восстановления выполните:${NC}"
            echo "  adb shell wm size reset"
            echo "  adb shell wm density reset"
            exit 0
            ;;
        
        *)
            echo -e "${RED}Неверная команда. Попробуйте снова.${NC}"
            ;;
    esac
done
