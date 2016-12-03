import java.io.{BufferedReader, File, FileReader}
import java.net.URL

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.Git
import play.api.libs.json.{JsObject, Json}

import scala.io.Source

/**
  * Created by singsand on 12/2/2016.
  */
case class getRepoMetadataJgit(id: String,htmlUrl: String)
case class repoCommitsWriter(repoName: String,repoId: String,commit_count: Int)
case class repoLanguageLOCWriter(repoName: String,repoId: String,language: String, lines:Long)
case class intermediateCase(userDetails: Array[String])
case class userDetailsWriter(userDetails: Array[String])


object mongoDbToMySqlObject {
  def main(args: Array[String]): Unit = {
    //    val inst: myClass = new myClass()
    //    inst.method(new Array[String](5))
    val inst: mongoDbToMySql = new mongoDbToMySql()
    inst.method(new Array[String](5))
  }
}

class mongoDbToMySql {

  def method(args: Array[String]): Unit = {
    println("In method")

    val system = ActorSystem("dbConnectorSystem")
    val mySqlWriterActor = system.actorOf(Props[mySqlWriterActor], name = "mySqlWriterActor")
    val getMetadataJgit = system.actorOf(Props(new getMetadataJgit(mySqlWriterActor)), name = "getMetadataJgit")

    val mongoDbReaderActor = system.actorOf(Props(new mongoDbReaderActor(getMetadataJgit)), name = "mongoDbReaderActor")

    mongoDbReaderActor ! "getListURL"
    mongoDbReaderActor ! "getUsersJSON"

  }
}

class mongoDbReaderActor(getMetadataJgit: ActorRef)  extends Actor {

  def receive = {
    case "getListURL" => {
      println("getListURL")
      val languages = List("java", "python", "go", "php", "scala", "c", "html", "cpp", "javascript", "csharp")
      for (language <- languages) {

        //make call to mongodb retriever method
        val repoDetailslist=MongoDBOperationAPIs.getHTMLURL(language+"Collection",4)

//        val list: List[String] = List(
//
//                      "https://github.com/captainriku75/bash_like_shell,123",
//                    "https://github.com/lixiangers/BadgeUtil,123"
//        )
        for (list_element <- repoDetailslist) {
          val htmlUrl=list_element.split(",")(0)
          val id=list_element.split(",")(1)
          getMetadataJgit ! getRepoMetadataJgit(id,htmlUrl)
        }
      }
    }

    case "getUsersJSON" =>{

      val userDetailsList=MongoDBOperationAPIs.getUserDetails()

//      val userDetailsList=List(
//  "libin1987,22090870,6,1,2,https://api.github.com/users/libin1987/subscriptions",
//  "monstertony,20313408,5,4,3,https://api.github.com/users/monstertony/subscriptions")

      //loginname,id,repos#,followers#,following#,subscription_url

      for(temp<-userDetailsList)
        {
          val userDetails=temp.split(",")
//          println(userDetails(5))
          val connection = new URL(userDetails(5)).openConnection
          connection.setRequestProperty(HttpBasicAuth.AUTHORIZATION, HttpBasicAuth.getHeader("ssingh72cs441", "441cloud"))
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
            getMetadataJgit ! intermediateCase(userDetails)
          }
          catch{case e: Exception =>

            //add exception handling for user not found type exception and seperate for limit reached exception
            response="";
            println("Exception: "+userDetails(0))
          }



        }

      //      getMetadataJgit.recursiveListFiles(new File("../userJsons"))
    }
  }
}
class getMetadataJgit(mySqlWriterActor: ActorRef)  extends Actor {

  val language = List("java", "python", "go", "php", "scala", "c", "html", "cpp", "javascript", "csharp")
  val language_extensions = List("java", "py", "go", "php", "scala", "c", "html","htm", "cpp", "js", "cs")


  def recursiveListFiles(f: File): Array[File] = {

    val currentFiles = f.listFiles

    val filtered_files = currentFiles.filter(f => """.*\.(java|py|go|php|scala|c|html|htm|cpp|js|cs)$""".r.findFirstIn(f.getName).isDefined)
    filtered_files ++ currentFiles.filter(_.isDirectory).flatMap(recursiveListFiles(_))

    /* the documents indexed are only the code files with the below extensions*/
    //    currentFiles ++ currentFiles.filter(_.isDirectory).flatMap(recursiveListFiles(_))
  }

  def receive = {

    case getRepoMetadataJgit(id: String,htmlUrl: String) => {
      //      println("In getRepoMetadataJgit")
      //      println(htmlUrl)

      val repoName=htmlUrl.split("/")(htmlUrl.split("/").length-1)

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


      var language_loc_map = scala.collection.mutable.Map[String, Long]()

      val list_files=recursiveListFiles(new File("../"++repoName))
      val file_count=list_files.length

      mySqlWriterActor ! repoCommitsWriter(repoName,id,commit_count)

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
            language_loc_map += (language -> lines)
          }
        else
          {
            language_loc_map(language)=language_loc_map(language)+lines
          }

      }


      language_loc_map foreach {case (language, loc) => mySqlWriterActor ! repoLanguageLOCWriter(repoName,id,language, loc)}

      try {
        git.getRepository.close()
        FileUtils.forceDelete(new File("../"+repoName));
      }

      catch{case e: Exception => println("Exception: "+e)}

    }
    case intermediateCase(userDetails: Array[String]) => {


      mySqlWriterActor ! userDetailsWriter(userDetails)
    }
    }

}

class mySqlWriterActor extends Actor {

  def receive = {
    case repoCommitsWriter(repoName: String,repoId: String,commit_count: Int) => {

      println(repoName+  " "+repoId+ " "+commit_count)
    }

    case repoLanguageLOCWriter(repoName: String,repoId: String,language: String, lines:Long) => {
      println(repoName+  " "+repoId+ " "+language+" "+lines)
    }

    case userDetailsWriter(userDetails: Array[String]) => {

      println(userDetails.foreach(element=>print(element+"  ")))
    }
  }
}

