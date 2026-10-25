package com.kampplus.havali.domain.model

/**
 * Ulaşım şekli. Her mod kendi ortalama hızını ve havaya karşı hassasiyetini taşır;
 * böylece "motor mu araba mı" sorusu için if zinciri yazmak gerekmez.
 *
 * @param averageKmh Şehir içi ortalama hız; yol süresi tahmini bunu kullanır.
 * @param riskSensitivity Yol riski çarpanı. Motor ve bisiklet ıslak zemine daha hassastır.
 * @param commute Günlük ulaşımda (Yol / Hazırlık ekranı) seçilebilir mi.
 * @param trip Şehirler arası yolculukta (Valiz) seçilebilir mi.
 */
enum class TravelMode(
    val averageKmh: Double,
    val riskSensitivity: Double,
    val commute: Boolean,
    val trip: Boolean
) {
    WALK(averageKmh = 4.5, riskSensitivity = 0.7, commute = true, trip = false),
    BIKE(averageKmh = 15.0, riskSensitivity = 1.3, commute = true, trip = false),
    MOTORBIKE(averageKmh = 28.0, riskSensitivity = 1.4, commute = true, trip = true),
    CAR(averageKmh = 24.0, riskSensitivity = 1.0, commute = true, trip = true),
    TRANSIT(averageKmh = 18.0, riskSensitivity = 0.6, commute = true, trip = false),
    PLANE(averageKmh = 500.0, riskSensitivity = 0.3, commute = false, trip = true),
    BUS(averageKmh = 75.0, riskSensitivity = 0.6, commute = false, trip = true),
    TRAIN(averageKmh = 90.0, riskSensitivity = 0.4, commute = false, trip = true);

    companion object {
        val commuteModes: List<TravelMode> = entries.filter { it.commute }
        val tripModes: List<TravelMode> = entries.filter { it.trip }
    }
}
