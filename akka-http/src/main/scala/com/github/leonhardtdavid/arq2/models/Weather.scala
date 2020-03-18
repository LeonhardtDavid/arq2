package com.github.leonhardtdavid.arq2.models

/**
  * Weather model.
  *
  * @param temperature Temperature for the day.
  * @param pressure    Pressure for the day.
  * @param humidity    Humidity for the day.
  * @param weather     Weather.
  * @param description Weather desciption.
  */
case class Weather(temperature: Float, pressure: Float, humidity: Float, weather: String, description: String)
