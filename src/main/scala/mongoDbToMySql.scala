import java.io.File

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.Git

/**
  * Created by singsand on 12/2/2016.
  */
case class getRepoMetadataJgit(htmlUrl: String)
case class repoCommitsWriter(repoName: String,repoId: String,commit_count: Int)
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
//        val list=MongoDBOperationAPIs.getHTMLURL(language+"Collection",4)

        val list: List[String] = List(
          "https://github.com/captainriku75/bash_like_shell",
          "https://github.com/lixiangers/BadgeUtil"
        )
        for (htmlUrl <- list) {
          getMetadataJgit ! getRepoMetadataJgit(htmlUrl)
        }
      }
    }
  }}
class getMetadataJgit(mySqlWriterActor: ActorRef)  extends Actor {
  def receive = {

    case getRepoMetadataJgit(htmlUrl: String) => {
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

      mySqlWriterActor ! repoCommitsWriter(repoName,"0",commit_count)

      val language_extensions = List("java", "python", "go", "php", "scala", "c", "html", "cpp", "javascript", "csharp")






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
  }
}

