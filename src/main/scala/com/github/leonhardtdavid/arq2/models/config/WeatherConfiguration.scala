package com.github.leonhardtdavid.arq2.models.config

/**
  * Weather configurations.
  *
  * @param url Weather service url.
  */
class WeatherConfiguration(url: String) {

  /**
    * Generates the url for the given parameters.
    *
    * @param city    City name.
    * @param country Country name.
    * @param days    Days quantity.
    * @return A replaced url.
    */
  def urlFor(city: String, country: String, days: Int): String =
    this.url
      .replace("{0}", s"$city, $country")
      .replace("{1}", days.toString)
      .replaceAll(" ", "+")

}
