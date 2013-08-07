package com.github.jackcviers

import org.specs2.Specification
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration._

class ListExampleSpec extends Specification {

  def is = s2"""
    This is an example of using Futures with immutable Lists.

    A future of an immutable List should
      Return the entire list $entireList
      Return the list with the first three elements removed $removeFirstThree
      Prepend 6 and 7 to the last three elements $prepend6And7
      Prepend 9 and 8 to the last element of the list $prepend9And8
      and Leave the original list untouched $originalList
"""
    val list:List[Int] = List(1,2,3,4,5)
    val expectedEntireList:List[Int] = List(1,2,3,4,5)
    val expectedRemovedList:List[Int] = List(4,5)
    def workWithEntireList(l: List[Int]): Future[List[Int]] = Future { list }
    def workWithLastTwo(l: List[Int]): Future[List[Int]] = Future { list drop 3}
    def prepend6And7 = Future { 6 :: 7 :: (list drop 2)} must contain(allOf(6,7,3,4,5)).await
    def prepend9And8 = Future { 9 :: 8 :: (list drop 4)} must contain(allOf(9,8,5)).await
    def entireList = workWithEntireList(list) must contain(allOf(1,2,3,4,5)).await
    def removeFirstThree = workWithLastTwo(list) must contain(allOf(4,5)).await
    def originalList = {
      workWithEntireList(list) must contain(allOf(1,2,3,4,5)).await
      list must_== expectedEntireList
    }
}
