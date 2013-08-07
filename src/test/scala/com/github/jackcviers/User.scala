package com.github.jackcviers

import org.specs2.Specification

class UserSpec extends Specification { def is = s2"""

  This is a specification to check the 'User' class.

  The 'User' class should
    have a firstName $firstName
    have a lastName $lastName
  """
  val first = "Test"
  val last = "User"
  def firstName = User(first, last).firstName must_== first
  def lastName = User(first, last).lastName must_== last

}










