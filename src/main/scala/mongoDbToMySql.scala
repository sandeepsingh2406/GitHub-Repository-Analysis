import akka.actor.{Actor, ActorRef, ActorSystem, Props}

/**
  * Created by singsand on 12/2/2016.
  */
case class getRepoMetadataJgit(htmlUrl: String)

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
    val mongoDbReaderActor = system.actorOf(Props(new mongoDbReaderActor(mySqlWriterActor)), name = "mongoDbReaderActor")

    mongoDbReaderActor ! "getListURL"
  }
}

class mongoDbReaderActor(mySqlWriterActor: ActorRef)  extends Actor {

  def receive = {
    case "getListURL" => {
      println("getListURL")
      val languages = List("java", "python", "go", "php", "scala", "c", "html", "cpp", "javascript", "csharp")
      for (language <- languages) {

        //make call to mongodb retriever method
        val list: List[String] = List(
          "https://github.com/HotBitmapGG/bilibili-android-client",
          "https://github.com/Hitomis/SortRichEditor",
          "https://github.com/smuyyh/SprintNBA"
        )
        for (htmlUrl <- list) {
          self ! getRepoMetadataJgit   (htmlUrl)
        }
      }
      }

    case getRepoMetadataJgit(htmlUrl: String) => {
      println("In getRepoMetadataJgit")

      val repoName=htmlUrl.split("/")(-1)
      println(repoName)
//      val git  = Git.cloneRepository()
//        .setURI( "https://github.com/captainriku75/CineMango_CSE308" ).setDirectory(new File("../CineMango_CSE308"))
//        .call();
    }
  }
}

class mySqlWriterActor extends Actor {

  def receive = {
    case "downloadRepo" => {
    }
  }
}

