import org.scalatest.FunSuite

/**
  * Created by singsand on 12/4/2016.
  */
//This test case requires indices to be already created(in particular, java index), so that it can check the web service is retrieving the correct results

class WebServiceTest extends FunSuite {


  val thread = new Thread(new Runnable {
    def run() {

      //Creating object of SearchEngine1 class to start the web service
      val inst: WebService = new WebService()
      inst.method(new Array[String](5))
    }
  })

  //Starting the web service
  thread.start()
  println(s"Waiting 10 seconds for web service to start in another thread. Some output from other class might be visible here.")

  Thread.sleep(10000)
  println(s"Web service has been started in another thread.")


//  test("Check message returned when web service homepage is called") {
//
//
//    println("Now calling web service at http://127.0.0.1:8080 and checking response.")
//
//    var url = "http://127.0.0.1:8080/"
//    var result = scala.io.Source.fromURL(url).mkString
//    val Pattern = "<h1>Welcome to the Web Service</h1>".r
//    val matched_result = Pattern.findFirstIn(result.toString).getOrElse("no match")
//
//    //Get response from web service
//
//    thread.interrupt()
//
//    assert(matched_result.equals("<h1>Welcome to the Web Service</h1>"))
//  }

  test("Check response returned when web service is called with parameters topUsers=1")
  {

    println("This unit test case requires tables to be already created and populated in mysql," +
      " so that it can check the web service is retrieving the correct results.\n Please make sure that data already exists.\n\n")

    println("Now calling web service at http://127.0.0.1:8080 with parameter topUsers=1 and checking response.")

    var url = "http://127.0.0.1:8080/?topUsers=1"
    var result = scala.io.Source.fromURL(url).mkString
    //Get response from web service

//    val Pattern = "ms</b><br><br><b>Result # [0-9]*".r
//    val matched_result=Pattern.findFirstIn(result.toString).getOrElse("no match")
    println(result)
    //Match result to match pattern to check later

    //Stop the web service
    thread.interrupt()
//    assert(matched_result.equals("ms</b><br><br><b>Result # 1"))

  }


}