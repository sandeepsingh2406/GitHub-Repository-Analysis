import java.io.{BufferedWriter, File, FileWriter}
import java.net.URL

import org.apache.commons.codec.binary.Base64
import play.api.libs.json._

import scala.io.Source
/**
  * Created by singsand on 11/29/2016.
  */

//Object which creates instance of class contain the main code
object githubProcessor {
  def main(args: Array[String]): Unit = {
    val inst: myClass = new myClass()
    inst.method(new Array[String](5))
  }
}


class myClass() {


  def method(args: Array[String]): Unit = {
    var response: String=""
    val languages = List("java","python","go","php","scala","c","html","cpp","javascript","csharp")

    val dir:File = new File("downloadedfiles");
    val dates= List(1,6,11,16,21,26)
    var tempdatestring:String=""
    var nextdatestring:String=""

    var start_time: Long = System.currentTimeMillis / 1000

    var iteration:Int=1


    dir.mkdir()
    try {
      for (language <- languages) {
        //        println(i)
        var url = ""
        val dir:File = new File("downloadedfiles/"+language);
        dir.mkdir()



        for(tempdateint <- dates)
        {
          val nextdateint=tempdateint+4
          if(tempdateint<10) {tempdatestring="0"+tempdateint.toString}
          else{tempdatestring=tempdateint.toString}

          if(nextdateint<10) {nextdatestring="0"+nextdateint.toString}
          else{nextdatestring=nextdateint.toString}


          var pagenum:Int=1


          url = "https://api.github.com/search/repositories?q=language:" + language + "+"+"created:2016-06-"+
            tempdatestring+"..2016-06-"+nextdatestring+"+size:%3E10000"
          //            println(url)
          val connection = new URL(url).openConnection
          connection.setRequestProperty(HttpBasicAuth.AUTHORIZATION, HttpBasicAuth.getHeader("ssingh72cs441", "441cloud"))
          response = Source.fromInputStream(connection.getInputStream).mkString
          val json_response=Json.parse(response.toString)
          val total_hits=Integer.parseInt(((json_response \ "total_count")).toString())


          iteration=iteration+1


          if(iteration>=31)
          {
            println("taking a rest")

            start_time=(System.currentTimeMillis / 1000)
            while((System.currentTimeMillis / 1000)<(start_time+61))
            {
            }
            start_time=(System.currentTimeMillis / 1000)
            iteration=1
          }



          while((pagenum<11) && (pagenum<=((total_hits/100).toInt)+1))
          {

            url = "https://api.github.com/search/repositories?q=language:" + language + "+"+"created:2016-06-"+
              tempdatestring+"..2016-06-"+nextdatestring+"+size:%3E10000"+"&per_page=100&page="+pagenum

            val connection = new URL(url).openConnection
            connection.setRequestProperty(HttpBasicAuth.AUTHORIZATION, HttpBasicAuth.getHeader("ssingh72cs441", "441cloud"))
            response = Source.fromInputStream(connection.getInputStream).mkString


            iteration=iteration+1


            val file = new File("downloadedfiles/"+language+"/"+language+"_"+"2016-06-"+
              tempdatestring+"_2016-06-"+nextdatestring+"_page"+pagenum+".json")
            val bw = new BufferedWriter(new FileWriter(file))
            bw.write(response)
            bw.close()


            if(iteration>=31)
            {
              println("taking a rest")

              start_time=(System.currentTimeMillis / 1000)
              while((System.currentTimeMillis / 1000)<(start_time+61))
              {
              }
              start_time=(System.currentTimeMillis / 1000)
              iteration=1
            }

            pagenum=pagenum+1
          }

        }
      }
    }
    catch {
      case e: Exception => println(e);
    }



    //    val url = "https://api.github.com/search/repositories?q=language:" + "java" + "+created:2016-01-01..2016-01-05+size:%3E10000"
    //            val connection = new URL(url).openConnection
    //            connection.setRequestProperty(HttpBasicAuth.AUTHORIZATION, HttpBasicAuth.getHeader("ssingh72cs441", "441cloud"))
    //            response = Source.fromInputStream(connection.getInputStream).mkString
    //    val json_response=Json.parse(response.toString)
    //    val total_hits=((json_response \ "total_count"))
    //                println(total_hits)


    //    val dir:File = new
    // File("downloadedfiles");
    //
    //    // attempt to create the directory here
    //    dir.mkdir();
    //    val file = new File("downloadedfiles/file2.json")
    //    val bw = new BufferedWriter(new FileWriter(file))
    //    bw.write(response)
    //    bw.close()
  }
}


object HttpBasicAuth {
  val BASIC = "Basic";
  val AUTHORIZATION = "Authorization";

  def encodeCredentials(username: String, password: String): String = {
    new String(Base64.encodeBase64String((username + ":" + password).getBytes));
  };

  def getHeader(username: String, password: String): String =
    BASIC + " " + encodeCredentials(username, password);

};