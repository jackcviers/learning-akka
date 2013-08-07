package com.github.jackcviers

sealed trait ExampleMessage
case class Gamma(g: String) extends ExampleMessage
case class Beta(b: String, g: Gamma) extends ExampleMessage
case class Alpha(b1: Beta, b2: Beta) extends ExampleMessage
