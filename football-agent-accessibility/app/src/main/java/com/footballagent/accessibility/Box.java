package com.footballagent.accessibility;

final class Box {
    final int left;
    final int top;
    final int right;
    final int bottom;

    Box(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    int width() { return Math.max(0, right - left); }
    int height() { return Math.max(0, bottom - top); }
    int centerY() { return top + height() / 2; }

    double verticalOverlapRatio(Box other) {
        int intersection = Math.max(0, Math.min(bottom, other.bottom) - Math.max(top, other.top));
        int denominator = Math.max(1, Math.min(height(), other.height()));
        return intersection / (double) denominator;
    }
}
