package com.github.jackcviers

trait Person {
  def firstName: String
  def lastName: String
}

case class User(firstName: String, lastName: String) extends Person
