import java.io.{BufferedReader, File, FileReader}
import java.net.URL

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import grizzled.slf4j.Logger
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.Git
import play.api.libs.json.{JsObject, Json}

import scala.io.Source

/**
  * Created by singsand on 12/2/2016.
  */
case class getRepoMetadataJgit(id: String,htmlUrl: String)
case class topRepoCommitsWriter(repoName: String,repoId: String,commit_count: Int,total_file_count: Int)
case class topRepoLanguageWriter(repoName: String,repoId: String,language: String, lines:Long, numfile:Long)
case class intermediateCase(userDetails: Array[String])
case class userDetailsWriter(userDetails: Array[String])
case class allLanguageRepoWriter(repoDetails: List[String])
case class intermediateCaseForRepo(singleRepoAllDetails: List[String])

object mongoDbToMySqlObject {
  def main(args: Array[String]): Unit = {
    //    val inst: myClass = new myClass()
    //    inst.method(new Array[String](5))
    val inst: mongoDbToMySql = new mongoDbToMySql()
    inst.method(new Array[String](5))
  }
}

class mongoDbToMySql {

  //Initiate a logger
  val logger = Logger("mongoDbToMySql")

  def method(args: Array[String]): Unit = {
    println("In method")

    logger.info("Initiating mongoDbToMySql class")

    logger.info("Creating actor system and different actors")

    //create actor system and actors

    val system = ActorSystem("dbConnectorSystem")
    val mySqlWriterActor = system.actorOf(Props[mySqlWriterActor], name = "mySqlWriterActor")
    val getMetadataJgit = system.actorOf(Props(new getMetadataJgit(mySqlWriterActor)), name = "getMetadataJgit")

    val mongoDbReaderActor = system.actorOf(Props(new mongoDbReaderActor(getMetadataJgit)), name = "mongoDbReaderActor")

    //use all actors to transfer differernt kind of data from mongodb to mysql
    mongoDbReaderActor ! "getListURL"
    //
    mongoDbReaderActor ! "getRepoAllDetails"
    mongoDbReaderActor ! "getUsersJSON"
    //
  }
}

class mongoDbReaderActor(getMetadataJgit: ActorRef)  extends Actor {

  //Initiate a logger
  val logger = Logger("mongoDbToMySql")

  def receive = {
    case "getListURL" => {
      logger.info("In getListURL actor, will also attempt to store stuff to MySQL")
      val languages = List("java", "python", "go", "php", "scala", "c", "html", "cpp", "javascript", "csharp")
      for (language <- languages) {

        //make call to mongodb retriever method
        val repoDetailslist=MongoDBOperationAPIs.getHTMLURL(language+"Collection",3)

        //        val repoDetailslist: List[String] = List(
        //
        //                      "https://github.com/md100play/PodTube,123",
        //                    "https://github.com/alexrohleder96/caronas4colonia,345"
        //        )
        for (list_element <- repoDetailslist) {
          val htmlUrl=list_element.split(",")(0)
          val id=list_element.split(",")(1)
          getMetadataJgit ! getRepoMetadataJgit(id,htmlUrl)
        }
      }
    }

    case "getRepoAllDetails" => {
      logger.info("In getRepoAllDetails, will attempt to store data into mysql   ")

      val languages = List("java", "python", "go", "php", "scala", "c", "html", "cpp", "javascript", "csharp")
      for (language <- languages) {
        {


          //make call to mongodb retriever method
          val repoAllDetailslist=MongoDBOperationAPIs.getRepoDetails(language+"Collection")
          println("got response from mongodb with all repo details")

          //          val repoAllDetailslist: List[List[String]] = List(
          //            List("bash_like_shell", "61180730", "captainriku75", "19618265", "2016-06-15", "2016-06-15", "1", "2", "3","100"),
          //              List("abcde", "1234", "repoabcd", "3456", "2017-06-15", "2017-06-15", "2", "3", "4","200")
          //
          //          )
          for(singleRepoAllDetails<-repoAllDetailslist)
          {
            println("getRepoAllDetails->intermediateCaseForRepo")
            getMetadataJgit ! intermediateCaseForRepo(singleRepoAllDetails)

          }

        }

      }
    }

    case "getUsersJSON" =>{

      logger.info("In getUsersJson, will store user details into MySQL")


      val userDetailsList=MongoDBOperationAPIs.getUserDetails()
      logger.info("Got response from mongodb with all user details")

      //      val userDetailsList=List(
      //  "libin1987,22090870,6,1,2,https://api.github.com/users/libin1987/subscriptions",
      //  "monstertony,20313408,5,4,3,https://api.github.com/users/monstertony/subscriptions")

      //loginname,id,repos#,followers#,following#,subscription_url

      var count=0
      for(temp<-userDetailsList)
      {
        val userDetails=temp.split(",")
        //          println(userDetails(5))

        //get user github url and make api call using it
        val connection = new URL(userDetails(5)).openConnection
        connection.setRequestProperty(HttpBasicAuth.AUTHORIZATION, HttpBasicAuth.getHeader("clouduic", "Test_123"))
        var response=""
        try {
          response = Source.fromInputStream(connection.getInputStream).mkString
          if(response.replace("[","").replace("]","").isEmpty)
          {
            userDetails(5)="0"
          }
          else {
            val json_response = Json.parse(response.toString)
            userDetails(5)=json_response.as[List[JsObject]].size.toString


          }
          println("Calling intermediatecase")
          getMetadataJgit ! intermediateCase(userDetails)
        }
        catch{case e: Exception =>

          //add exception handling for user not found type exception and seperate for limit reached exception
          response="";
          println("Exception: "+userDetails(0))
          logger.error("Exception in getUsersJSON case in mongoDbReaderActor actor: "+e.getMessage)
        }

        count = count + 1

        //to not cross the 5000 requests api call limit per hour
        if (count >= 4950)
        {
          println("taking a rest from downloading user json")

          var start_time = (System.currentTimeMillis / 1000)
          while ((System.currentTimeMillis / 1000) < (start_time + 3600)) {
          }
          start_time = (System.currentTimeMillis / 1000)
          count = 0


        }



      }

      //      getMetadataJgit.recursiveListFiles(new File("../userJsons"))
    }
  }
}


//use jgit to get some metadata about repo
class getMetadataJgit(mySqlWriterActor: ActorRef)  extends Actor {


  //Initiate a logger
  val logger = Logger("mongoDbToMySql")

  val language = List("java", "python", "go", "php", "scala", "c", "html", "cpp", "javascript", "csharp")
  val language_extensions = List("java", "py", "go", "php", "scala", "c", "html","htm", "cpp", "js", "cs")


  //below return will return all files in the current repo that are in the expected formats

  def recursiveListFiles(f: File): Array[File] = {

    val currentFiles = f.listFiles

    val filtered_files = currentFiles.filter(f => """.*\.(java|py|go|php|scala|c|html|htm|cpp|js|cs)$""".r.findFirstIn(f.getName).isDefined)

    try {
      filtered_files ++ currentFiles.filter(_.isDirectory).flatMap(recursiveListFiles(_))
    }
    catch{case e:Throwable => return filtered_files}
    /* the documents indexed are only the code files with the below extensions*/
    //    currentFiles ++ currentFiles.filter(_.isDirectory).flatMap(recursiveListFiles(_))
  }

  def receive = {

    case getRepoMetadataJgit(id: String,htmlUrl: String) => {
      //      println("In getRepoMetadataJgit")
      //      println(htmlUrl)
      println(htmlUrl)

      val repoName=htmlUrl.split("/")(htmlUrl.split("/").length-1)

      println(MySQLOperationAPIs.checkRow(repoName))

      if (!MySQLOperationAPIs.checkRow(repoName))
      {
        //use jgit to clone repo, then later delete it
        val git  = Git.cloneRepository()
          .setURI( htmlUrl ).setDirectory(new File("../"+repoName))
          .call();

        val commits  = git.log().call();
        val iter=commits.iterator()
        var commit_count = 0;
        while(iter.hasNext)
        {
          iter.next()
          commit_count=commit_count+1
        }


        var language_loc_map = scala.collection.mutable.Map[String, List[Long]]()

        val list_files=recursiveListFiles(new File("../"++repoName))
        val total_file_count=list_files.length

        mySqlWriterActor ! topRepoCommitsWriter(repoName,id,commit_count,total_file_count)


        //get lines and files count for files of difeferent languages in current repo
        for(file<-list_files)
        {

          val filename=file.getName
          val file_extension= filename.split("\\.")(filename.split("\\.").length-1)
          var language=""
          if(file_extension.equals("cs"))language="csharp"
          else if( file_extension.equals("htm"))language="html"
          else if( file_extension.equals("js"))language="javascript"
          else if( file_extension.equals("py"))language="python"
          else language=file_extension

          val reader = new BufferedReader(new FileReader(file));
          var lines :Long= 0;
          while (reader.readLine() != null) {lines=lines+1}
          reader.close();

          if(!language_loc_map.contains(language))
          {
            language_loc_map += (language -> List(lines,1))
          }
          else
          {
            language_loc_map(language)=List(language_loc_map(language)(0)+lines,language_loc_map(language)(1)+1)
          }

        }


        language_loc_map foreach {case (language, list) => mySqlWriterActor ! topRepoLanguageWriter(repoName,id,language, list(0),list(1))}

        try {
          git.getRepository.close()
          FileUtils.forceDelete(new File("../"+repoName));
        }

        catch{case e: Exception => println("Exception: "+e)
          logger.error("Exception in getRepoMetadataJgit case in getMetadataJgit actor: "+e.getMessage)

        }


      } else
      {println("Already exists: "+repoName)
      }

    }
    case intermediateCase(userDetails: Array[String]) => {


      mySqlWriterActor ! userDetailsWriter(userDetails)
    }

    case intermediateCaseForRepo(singleRepoAllDetails: List[String])=>{

      println("intermediateCaseForRepo->allLanguageRepoWriter")

      mySqlWriterActor !  allLanguageRepoWriter(singleRepoAllDetails)
    }
  }


}

//write rows to mysql to different tables using different cases
class mySqlWriterActor extends Actor {

  def receive = {
    case topRepoCommitsWriter(repoName: String,repoId: String,commit_count: Int,total_file_count: Int) => {

      println(repoName+  " "+repoId+ " "+commit_count+" "+total_file_count)
      MySQLOperationAPIs.insertTopRepoCommitsTable(repoName,repoId.toLong,commit_count,total_file_count)
    }

    case topRepoLanguageWriter(repoName: String,repoId: String,language: String, lines:Long, numfiles: Long) => {
      println(repoName+  " "+repoId+ " "+language+" "+lines+" "+numfiles)

      MySQLOperationAPIs.insertTopRepoLanguageTable(repoName,repoId.toLong,language,lines,numfiles.toInt)

    }


    case userDetailsWriter(userDetails: Array[String]) => {

      println(userDetails.foreach(element=>print(element+"  ")))
      MySQLOperationAPIs.insertUserTable(userDetails(0),
        userDetails(1).toLong,
        userDetails(2).toInt,
        userDetails(3).toInt,
        userDetails(4).toInt,
        userDetails(5).toInt)


    }

    case allLanguageRepoWriter(repoDetails: List[String]) => {

      println(repoDetails.foreach(element=>print(element+"  ")))

      MySQLOperationAPIs.insertAllLanguageRepoTable(repoDetails(0),repoDetails(1).toLong,repoDetails(2),repoDetails(3).toLong,
        repoDetails(4),repoDetails(5),repoDetails(6).toInt,repoDetails(7).toInt,repoDetails(8).toInt,repoDetails(9).toInt)

    }
  }
}

