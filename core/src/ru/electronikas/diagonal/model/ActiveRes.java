package ru.electronikas.diagonal.model;

/**
 * Persisted active resources (preferences keys).
 *
 * Removed in P0-10 (billing/adware cleanup):
 *  - noAdsTime  (was: end-of-paid-ad-removal timestamp)
 *  - isAdware   (was: true while ads are suppressed by paid purchase)
 */
public enum ActiveRes {
    record,
    gameFieldType
}
