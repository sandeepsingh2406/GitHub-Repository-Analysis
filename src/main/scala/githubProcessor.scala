import java.io.{BufferedWriter, File, FileWriter}
import java.net.URL
import java.nio.file.{Files, Paths}

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import org.apache.commons.codec.binary.Base64
import play.api.libs.json._

import scala.io.Source
/**
  * Created by singsand on 11/29/2016.
  */

case class parseRepoJson(file: File)
case class downloadUserJson(user:Set[String])



//Object which creates instance of class contain the main code
object githubProcessor {
  def main(args: Array[String]): Unit = {
    //    val inst: myClass = new myClass()
    //    inst.method(new Array[String](5))
    val inst: initializerClass = new initializerClass()
    inst.method(new Array[String](5))
  }
}

class initializerClass(){
  def method(args: Array[String]): Unit = {

    val dir: File = new File("../repoFiles");
    dir.mkdir()
    val system = ActorSystem("ActorSystem")
    val jsonParser = system.actorOf(Props[jsonParser], name = "jsonParser")
    val downloaderActor = system.actorOf(Props(new downloaderActor(jsonParser)), name = "downloaderActor")

    //        downloaderActor ! "downloadRepo"

    downloaderActor ! "fileRepoProcessor"
  }
}

class downloaderActor(jsonParser: ActorRef)  extends Actor {
  var downloadedUsers: Set[String] = Set()


  /* this function is use to iterate through all folder and fetch files with the extensions */
  def recursiveListFiles(f: File): Array[File] = {

    val currentFiles = f.listFiles

    val filtered_files = currentFiles.filter(f => """.*\.json$""".r.findFirstIn(f.getName).isDefined)
    filtered_files ++ currentFiles.filter(_.isDirectory).flatMap(recursiveListFiles(_))

    /* the documents indexed are only the code files with the below extensions*/
    //    currentFiles ++ currentFiles.filter(_.isDirectory).flatMap(recursiveListFiles(_))
  }

  def receive = {

    case "downloadRepo" => {
      println("In downloadRepo")
      var response: String = ""
      val languages = List("java", "python", "go", "php", "scala", "c", "html", "cpp", "javascript", "csharp")
      val dir: File = new File("../downloadedfiles");
      val dates = List(1, 6, 11, 16, 21, 26)
      var tempdatestring: String = ""
      var nextdatestring: String = ""
      var start_time: Long = System.currentTimeMillis / 1000

      var iteration: Int = 1


      dir.mkdir()


      for (language <- languages) {
        //        println(i)
        var url = ""
        val dir: File = new File("../downloadedfiles/" + language);
        dir.mkdir()




        for (tempdateint <- dates) {
          val nextdateint = tempdateint + 4
          if (tempdateint < 10) {
            tempdatestring = "0" + tempdateint.toString
          }
          else {
            tempdatestring = tempdateint.toString
          }

          if (nextdateint < 10) {
            nextdatestring = "0" + nextdateint.toString
          }
          else {
            nextdatestring = nextdateint.toString
          }


          var pagenum: Int = 1


          url = "https://api.github.com/search/repositories?q=language:" + language + "+" + "created:2016-06-" +
            tempdatestring + "..2016-06-" + nextdatestring + "+size:%3E10000"
          //            println(url)
          val connection = new URL(url).openConnection
          connection.setRequestProperty(HttpBasicAuth.AUTHORIZATION, HttpBasicAuth.getHeader("ssingh72cs441", "441cloud"))
          response = Source.fromInputStream(connection.getInputStream).mkString
          val json_response = Json.parse(response.toString)
          val total_hits = Integer.parseInt(((json_response \ "total_count")).toString())


          iteration = iteration + 1


          if (iteration >= 31) {
            println("taking a rest")

            start_time = (System.currentTimeMillis / 1000)
            while ((System.currentTimeMillis / 1000) < (start_time + 61)) {
            }
            start_time = (System.currentTimeMillis / 1000)
            iteration = 1
          }



          while ((pagenum < 11) && (pagenum <= ((total_hits / 100).toInt) + 1)) {

            url = "https://api.github.com/search/repositories?q=language:" + language + "+" + "created:2016-06-" +
              tempdatestring + "..2016-06-" + nextdatestring + "+size:%3E10000" + "&per_page=100&page=" + pagenum

            val connection = new URL(url).openConnection
            connection.setRequestProperty(HttpBasicAuth.AUTHORIZATION, HttpBasicAuth.getHeader("ssingh72cs441", "441cloud"))
            response = Source.fromInputStream(connection.getInputStream).mkString


            iteration = iteration + 1


            val file = new File("../downloadedfiles/" + language + "/" + language + "_" + "2016-06-" +
              tempdatestring + "_2016-06-" + nextdatestring + "_page" + pagenum + ".json")
            val bw = new BufferedWriter(new FileWriter(file))
            bw.write(response)
            bw.close()
            jsonParser ! parseRepoJson(file)


            if (iteration >= 31) {
              println("taking a rest")

              start_time = (System.currentTimeMillis / 1000)
              while ((System.currentTimeMillis / 1000) < (start_time + 61)) {
              }
              start_time = (System.currentTimeMillis / 1000)
              iteration = 1
            }

            pagenum = pagenum + 1
          }

        }
      }
    }
    //this belwo case gets bypassed if we call "downloadrepo" directly
    case "fileRepoProcessor" => {
      println("In fileRepoProcessor")
      val files = recursiveListFiles(new File("../downloadedfiles1/"))

      //      val file=files(0)
      for (file <- files) {
        jsonParser ! parseRepoJson(file)
      }
      jsonParser ! "getUsersFromJson"


    }

    case downloadUserJson(users: Set[String]) => {

      var count = 0
      for (user <- users) {



        if (!downloadedUsers.contains(user)) {


          downloadedUsers = downloadedUsers + user

          val url = "https://api.github.com/users/" + user

          val connection = new URL(url).openConnection
          connection.setRequestProperty(HttpBasicAuth.AUTHORIZATION, HttpBasicAuth.getHeader("ssingh72cs441", "441cloud"))
          var response = Source.fromInputStream(connection.getInputStream).mkString

          println(response)
          //write to mongo db here

          count = count + 1
          if (count >= 4990) {
            println("taking a rest from downloading user json")

            var start_time = (System.currentTimeMillis / 1000)
            while ((System.currentTimeMillis / 1000) < (start_time + 1800)) {
            }
            start_time = (System.currentTimeMillis / 1000)
            count = 0


          }

        }
      }
    }
  }
}


class jsonParser extends Actor {

  var count = 0
  var users: Set[String] = Set()
 def incrementAndPrint { count += 1; }//println(count) }
  def addUser(user: String){
    users=users+user
    }
  def receive = {
    case parseRepoJson(file) =>{

      //in this case i can get one item json and directly call another case of another actor that writes the json to mongodb

//      println(file.getAbsolutePath)
      val json_response = Json.parse(new String(Files.readAllBytes(Paths.get(file.getAbsolutePath))))
      val language=file.getName.split("_")(0)
      val dir: File = new File("../repoFiles/" + language);
      dir.mkdir()

//      println((json_response\ "items").as[List[JsObject]].size)
      for(i <- 0 until (json_response\ "items").as[List[JsObject]].size) {
      val str:String=((json_response \ "items")(i)).toString()


        if(language.equals("java"))
        {
          val user: String=((json_response \ "items")(i) \ "owner" \ "login").toString()
          addUser(user.replaceAll("\"",""))
        }


//        val id:String=((json_response \ "items")(i) \ "id").toString()
//        val file = new File("../repoFiles/" + language + "/" + language + "_" +id+".json")
//        val bw = new BufferedWriter(new FileWriter(file))
//        bw.write(str)
//        bw.close()
      }
    }


  case "getUsersFromJson" =>{
    println("Set size= "+users.size)
    sender ! downloadUserJson(users)

  }
  }
}



//class myClass() {
//
//
//  def method(args: Array[String]): Unit = {
//    var response: String=""
//
//    val languages = List("java","python","go","php","scala","c","html","cpp","javascript","csharp")
//
//    val dir:File = new File("../downloadedfiles");
//    val dates= List(1,6,11,16,21,26)
//    var tempdatestring:String=""
//    var nextdatestring:String=""
//
//    var start_time: Long = System.currentTimeMillis / 1000
//
//    var iteration:Int=1
//
//
//    dir.mkdir()
//    try {
//      for (language <- languages) {
//        //        println(i)
//        var url = ""
//        val dir:File = new File("../downloadedfiles/"+language);
//        dir.mkdir()
//
//
//
//        for(tempdateint <- dates)
//        {
//          val nextdateint=tempdateint+4
//          if(tempdateint<10) {tempdatestring="0"+tempdateint.toString}
//          else{tempdatestring=tempdateint.toString}
//
//          if(nextdateint<10) {nextdatestring="0"+nextdateint.toString}
//          else{nextdatestring=nextdateint.toString}
//
//
//          var pagenum:Int=1
//
//
//          url = "https://api.github.com/search/repositories?q=language:" + language + "+"+"created:2016-06-"+
//            tempdatestring+"..2016-06-"+nextdatestring+"+size:%3E10000"
//          //            println(url)
//          val connection = new URL(url).openConnection
//          connection.setRequestProperty(HttpBasicAuth.AUTHORIZATION, HttpBasicAuth.getHeader("ssingh72cs441", "441cloud"))
//          response = Source.fromInputStream(connection.getInputStream).mkString
//          val json_response=Json.parse(response.toString)
//          val total_hits=Integer.parseInt(((json_response \ "total_count")).toString())
//
//
//          iteration=iteration+1
//
//
//          if(iteration>=31)
//          {
//            println("taking a rest")
//
//            start_time=(System.currentTimeMillis / 1000)
//            while((System.currentTimeMillis / 1000)<(start_time+61))
//            {
//            }
//            start_time=(System.currentTimeMillis / 1000)
//            iteration=1
//          }
//
//
//
//          while((pagenum<11) && (pagenum<=((total_hits/100).toInt)+1))
//          {
//
//            url = "https://api.github.com/search/repositories?q=language:" + language + "+"+"created:2016-06-"+
//              tempdatestring+"..2016-06-"+nextdatestring+"+size:%3E10000"+"&per_page=100&page="+pagenum
//
//            val connection = new URL(url).openConnection
//            connection.setRequestProperty(HttpBasicAuth.AUTHORIZATION, HttpBasicAuth.getHeader("ssingh72cs441", "441cloud"))
//            response = Source.fromInputStream(connection.getInputStream).mkString
//
//
//            iteration=iteration+1
//
//
//            val file = new File("../downloadedfiles/"+language+"/"+language+"_"+"2016-06-"+
//              tempdatestring+"_2016-06-"+nextdatestring+"_page"+pagenum+".json")
//            val bw = new BufferedWriter(new FileWriter(file))
//            bw.write(response)
//            bw.close()
//
//
//            if(iteration>=31)
//            {
//              println("taking a rest")
//
//              start_time=(System.currentTimeMillis / 1000)
//              while((System.currentTimeMillis / 1000)<(start_time+61))
//              {
//              }
//              start_time=(System.currentTimeMillis / 1000)
//              iteration=1
//            }
//
//            pagenum=pagenum+1
//          }
//
//        }
//      }
//    }
//    catch {
//      case e: Exception => println(e);
//    }
//
//
//
//    //    val url = "https://api.github.com/search/repositories?q=language:" + "java" + "+created:2016-01-01..2016-01-05+size:%3E10000"
//    //            val connection = new URL(url).openConnection
//    //            connection.setRequestProperty(HttpBasicAuth.AUTHORIZATION, HttpBasicAuth.getHeader("ssingh72cs441", "441cloud"))
//    //            response = Source.fromInputStream(connection.getInputStream).mkString
//    //    val json_response=Json.parse(response.toString)
//    //    val total_hits=((json_response \ "total_count"))
//    //                println(total_hits)
//
//
//    //    val dir:File = new
//    // File("../downloadedfiles");
//    //
//    //    // attempt to create the directory here
//    //    dir.mkdir();
//    //    val file = new File("../downloadedfiles/file2.json")
//    //    val bw = new BufferedWriter(new FileWriter(file))
//    //    bw.write(response)
//    //    bw.close()
//  }
//}


object HttpBasicAuth {
  val BASIC = "Basic";
  val AUTHORIZATION = "Authorization";

  def encodeCredentials(username: String, password: String): String = {
    new String(Base64.encodeBase64String((username + ":" + password).getBytes));
  };

  def getHeader(username: String, password: String): String =
    BASIC + " " + encodeCredentials(username, password);

}