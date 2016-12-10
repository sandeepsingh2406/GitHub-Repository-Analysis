import java.io.{BufferedWriter, File, FileWriter}
import java.net.URL
import java.nio.file.{Files, Paths}

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import grizzled.slf4j.Logger
import org.apache.commons.codec.binary.Base64
import play.api.libs.json._

import scala.io.Source
/**
  * Created by singsand on 11/29/2016.
  */

case class parseRepoJson(file: File)
case class downloadUserJson(user:Set[String])
case class repoUploader(jsonResponse: String,  language: String,id: String)
case class userUploader(jsonUser: String,user: String)


//Authorization for github api calls
object HttpBasicAuth {
  val BASIC = "Basic";
  val AUTHORIZATION = "Authorization";

  def encodeCredentials(username: String, password: String): String = {
    new String(Base64.encodeBase64String((username + ":" + password).getBytes));
  };

  def getHeader(username: String, password: String): String =
    BASIC + " " + encodeCredentials(username, password);

}

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
    //Initiate a logger
    val logger = Logger("githubProcessor")

    logger.info("Initialing gitHubProcessor")


    val dir: File = new File("../userJsons");
    dir.mkdir()

    logger.info("Creating different actors to download repo")

    //create our actor system and actors
    val system = ActorSystem("ActorSystem")
    val mongoDbConnector = system.actorOf(Props[mongoDbConnector], name = "mongoDbConnector")
    val jsonParser = system.actorOf(Props(new jsonParser(mongoDbConnector)), name = "jsonParser")
    val downloaderActor = system.actorOf(Props(new downloaderActor(jsonParser,mongoDbConnector)), name = "downloaderActor")

    downloaderActor ! "downloadRepo"

    //this below case gets bypassed if we call "downloadrepo" directly, below actor can used to read locally written repo json files and write to mongodb
    //ideally, it should be commented and not used, used only for testing purposes

    //        downloaderActor ! "fileRepoProcessor"


  }
}

class downloaderActor(jsonParser: ActorRef,mongoDbConnector: ActorRef)  extends Actor {
  var downloadedUsers: Set[String] = Set()
  //Initiate a logger
  val logger = Logger("githubProcessor")


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
      logger.info("In downloadRepo actor, which downloads repo and uses other actors to write to mongodb")
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



        //we get repo for one month, divide the search queries for every 5 days, to get a good amount of results, as only 100 results are returned in one search query
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

          //our search query api call for getting Nov, 2016 github projects
          url = "https://api.github.com/search/repositories?q=language:" + language + "+" + "created:2016-11-" +
            tempdatestring + "..2016-11-" + nextdatestring + "+size:%3E10000"
          //            println(url)
          val connection = new URL(url).openConnection
          connection.setRequestProperty(HttpBasicAuth.AUTHORIZATION, HttpBasicAuth.getHeader("ssingh72cs441", "441cloud"))
          response = Source.fromInputStream(connection.getInputStream).mkString
          val json_response = Json.parse(response.toString)
          val total_hits = Integer.parseInt(((json_response \ "total_count")).toString())
          //parsing the json response above

          iteration = iteration + 1

          //since github api limits the number of queries per minute, if limit is crossed, we wait for a minute to make any api calls
          if (iteration >= 31) {
            println("taking a rest")

            start_time = (System.currentTimeMillis / 1000)
            while ((System.currentTimeMillis / 1000) < (start_time + 61)) {
            }
            start_time = (System.currentTimeMillis / 1000)
            iteration = 1
          }


          //only 10 pages can be viewed in the search api call
          while ((pagenum < 11) && (pagenum <= ((total_hits / 100).toInt) + 1)) {

            url = "https://api.github.com/search/repositories?q=language:" + language + "+" + "created:2016-06-" +
              tempdatestring + "..2016-06-" + nextdatestring + "+size:%3E10000" + "&per_page=100&page=" + pagenum

            val connection = new URL(url).openConnection
            connection.setRequestProperty(HttpBasicAuth.AUTHORIZATION, HttpBasicAuth.getHeader("ssingh72cs441", "441cloud"))
            response = Source.fromInputStream(connection.getInputStream).mkString


            iteration = iteration + 1

            //download all results to this temporary directory, which is then used later to get all json for all results
            val file = new File("../downloadedfiles/" + language + "/" + language + "_" + "2016-06-" +
              tempdatestring + "_2016-06-" + nextdatestring + "_page" + pagenum + ".json")
            val bw = new BufferedWriter(new FileWriter(file))
            bw.write(response)
            bw.close()
            jsonParser ! parseRepoJson(file)


            //since github api limits the number of queries per minute, if limit is crossed, we wait for a minute to make any api calls

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

      jsonParser ! "getUsersFromJson"
    }
    //this below case gets bypassed if we call "downloadrepo" directly, below actor can used to read locally written repo json files and write to mongodb
    case "fileRepoProcessor" => {
      println("In fileRepoProcessor")
      val files = recursiveListFiles(new File("../downloadedfiles1/"))

      //            val file=files(0)
      for (file <- files) {
        jsonParser ! parseRepoJson(file)
      }
      jsonParser ! "getUsersFromJson"


    }

    case downloadUserJson(users: Set[String]) => {

      var count=0

      logger.info("In downloadUserJson, it extracts user names from repo jsons, gets user details from github and writes to mongodb")
      var start_time = (System.currentTimeMillis / 1000)

      //      val user=users.head

      //      val already_downloadedJSON=recursiveListFiles(new File("../userJsons"))


      ////In case we want to read all user json present locally and write them to mongodb
      //      for (userJson <- already_downloadedJSON) {
      //        val userjson_str=Json.parse(new String(Files.readAllBytes(Paths.get(userJson.getAbsolutePath)))).toString()
      //        println(userjson_str)
      //        mongoDbConnector ! userUploader(userjson_str,userJson.getName())


      ////to get user json through api calls
      for (user <- users) {
        if (!downloadedUsers.contains(user))
        //          &&           !already_downloadedJSON.contains(new File("..\\userJsons\\"+user+".json")))
        {
          //          println("TRUE: "+user)
          //          println("user: " + user + ", user.size: " + users.size)

          downloadedUsers = downloadedUsers + user
          //
          val url = "https://api.github.com/users/" + user
          //
          val connection = new URL(url).openConnection
          connection.setRequestProperty(HttpBasicAuth.AUTHORIZATION, HttpBasicAuth.getHeader("ssingh72cs441", "441cloud"))
          var response=""
          try {
            response = Source.fromInputStream(connection.getInputStream).mkString
          }
          catch{case e: Exception =>

            //add exception handling for user not found type exception and seperate for limit reached exception
            response="";
            logger.error("Exception in downloadUserJson case of downloaderActor: "+e.getMessage)
            println("Exception: "+user)
          }

          //
          //          ////to get user json and write locally
          //          val file = new File("../userJsons/" + user+".json")
          //          val bw = new BufferedWriter(new FileWriter(file))
          //          bw.write(response)
          //          bw.close()

          //        to write userjson to mongodb
          mongoDbConnector ! userUploader(response,user)
          //
          count = count + 1
          if (count >= 4990)
          {
            println("taking a rest from downloading user json")

            var start_time = (System.currentTimeMillis / 1000)
            while ((System.currentTimeMillis / 1000) < (start_time + 3600)) {
            }
            start_time = (System.currentTimeMillis / 1000)
            count = 0


          }

        }

      }
    }
  }
}


//above downloader actor calls below jsonparser actor to parse the json response and process it
class jsonParser(mongoDbConnector: ActorRef)  extends Actor {

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
      //      val dir: File = new File("../repoFiles/" + language);
      //      dir.mkdir()

      //      println((json_response\ "items").as[List[JsObject]].size)
      for(i <- 0 until (json_response\ "items").as[List[JsObject]].size) {
        val str:String=((json_response \ "items")(i)).toString()

        //get all owners of java based repos
        if(language.equals("java"))
        {
          val user: String=((json_response \ "items")(i) \ "owner" \ "login").toString()
          addUser(user.replaceAll("\"",""))
        }
        val id:String=((json_response \ "items")(i) \ "id").toString()

        mongoDbConnector ! repoUploader(str,language,id)
        //use the  third actor to upload repos to mongo db

        //In case we want to write all json into files locally
        //        val id:String=((json_response \ "items")(i) \ "id").toString()
        //        val file = new File("../repoFiles/" + language + "/" + language + "_" +id+".json")
        //        val bw = new BufferedWriter(new FileWriter(file))
        //        bw.write(str)
        //        bw.close()
      }
    }

    //get user names and suer firt actor to download details of these users through api calls to github
    case "getUsersFromJson" =>{
      println("Set size= "+users.size)
      sender ! downloadUserJson(users)

    }
  }
}


//below actors writes json to monogodb in appropriate collections
class mongoDbConnector  extends Actor {
  var usercount = 0
  var repocount = 0
  def receive = {

    //to upload repo json
    case repoUploader(jsonRepo: String, language: String, id: String) => {
      repocount=repocount+1
      if((repocount%50)==0)println("repo upload count: "+repocount)
      MongoDBOperationAPIs.insertStringJSON(language+ParameterConstants.collectionNameSuffix,jsonRepo)
      println("In repoUploader")
    }

    //to upload user json
    case userUploader(jsonUser: String,user: String) => {
      usercount=usercount+1
      if((usercount%50)==0)println("user upload count: "+usercount)

      MongoDBOperationAPIs.insertStringJSON(ParameterConstants.usersCollectionName,jsonUser)
      println("In userUploader")


    }
  }
}

//classs used for testing purposes, without actors to test functionality
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


