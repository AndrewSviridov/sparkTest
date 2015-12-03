package com.skcc.bds.test

object ScalaExamples {
  def main(args: Array[String]) {
    listExam()
    
  }
  
  def listExam(){
    var sum = 0
    println("Hello, world!")
    var dstreams:List[List[Int]] = List(List(1,2,3))
    var dstream:List[Int] = List(5,6)
    
    dstreams = dstreams :+ dstream.map((i:Int) => i * 2)

   // dstreams.foreach(sum += _) 
    println("Sum ddd: " + sum)
    
    //
    val days = List("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    // Make a list element-by-element
    val when = "AM" :: "PM" :: List()
    
    // Pattern match
    days match {
      case firstDay :: secondDay :: otherDays =>
        println("The first day of the week is: " + firstDay)
        println("The secondDay day of the week is: " + secondDay)
        println("The otherDays day of the week is: " + otherDays)
      case List() =>
        println("There don't seem to be any week days.")
    }
  }
}