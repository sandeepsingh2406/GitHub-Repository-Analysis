import java.io.{BufferedReader, File, FileReader}

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.Git

/**
  * Created by singsand on 12/2/2016.
  */
case class getRepoMetadataJgit(id: String,htmlUrl: String)
case class repoCommitsWriter(repoName: String,repoId: String,commit_count: Int)
case class repoLanguageLOCWriter(repoName: String,repoId: String,language: String, lines:Long)


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
  }
}

class mongoDbReaderActor(getMetadataJgit: ActorRef)  extends Actor {

  def receive = {
    case "getListURL" => {
      println("getListURL")
      val languages = List("java", "python", "go", "php", "scala", "c", "html", "cpp", "javascript", "csharp")
      for (language <- languages) {

        //make call to mongodb retriever method
        //        val map=MongoDBOperationAPIs.getHTMLURL(language+"Collection",4)

        var map = scala.collection.mutable.Map[String, String]()
        map +=
         ("1" -> "https://github.com/captainriku75/bash_like_shell")//"https://github.com/md100play/PodTube")
        map+=
          ("2" -> "https://github.com/captainriku75/simple_chat_program")//"https://github.com/alexrohleder96/caronas4colonia")

        for ((id,htmlUrl) <- map) {
          getMetadataJgit ! getRepoMetadataJgit(id,htmlUrl)
        }
      }
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
  }
}

