package com.example.smarcifier

class TemperatureHistory(size: Int) {
    private val maxSize: Int = size;
    private val history: ArrayList<Float> = ArrayList();

    fun push(value: Float) {
        history.add(value);
        if (history.size > maxSize) {
            history.removeFirst()
        }
    }

    fun clear() {
        history.clear()
    }

    fun sum(): Float {
        var sum = 0.0f;
        for (value in history) {
            sum += value;
        }
        return sum;
    }

    fun avg(): Float {
        return sum() / history.size;
    }
}