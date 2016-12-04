import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.io.StdIn

/**
  * Created by singsand on 12/3/2016.
  */

//Object which creates instance of class contain the main code

object WebService {
  def main(args: Array[String]): Unit = {
    val inst: WebService = new WebService()
    inst.method(new Array[String](5))
  }
}

  //This class creates web service and calls actors to process responses from web service
  class WebService() {
    def method(args: Array[String]): Unit = {

      //Inititate an actor system
      implicit val system = ActorSystem()
      implicit val materializer = ActorMaterializer()


      // needed for the future map/flatmap in the end
      implicit val executionContext = system.dispatcher

      //Create a handler for when web service responds with just URL, that is the homepage
      object Route1 {
        val route =
          path("") {
            akka.http.scaladsl.server.Directives.get {
              akka.http.scaladsl.server.Directives.get {
                complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Welcome to the Web Service</h1>"))
            }
            }
          }
      }
      object Route2 {
        val route =
          parameters('topUsers, 'sortBy ? "publicReposCount") { (topUsers,sortBy) =>
            var count=""
            if(topUsers==null) {count=10.toString}
            else count=topUsers.toString


            val list=MySQLOperationAPIs.topUsers(count, sortBy)
            if(list.size==1)
              {complete("Sorry not results found!")}
            else {

              var output=""
              val head=list(0)
              output+="[\n"
              list.remove(0)
              var i:Int=0
              for(item<-list)
              {
                i+=1

                output+="  {\n"
                var j:Int=0
                for(element<-item)
                {

                  output+="    \""+ head(j)+"\""
                  j+=1
                  output+=" : "
                  output+="\""+element.toString+"\""
                  if((j!=head.size)){output+=",\n"}
                }

                output+="\n  }"
                if(i!=list.size) output+=",\n"
              }
              output+="\n]"

              complete(output)
            }




//            complete(count)

          }}

      object Route3 {
        val route =
          parameters('topRepo) { (topRepo) =>
            var count=""
            if(topRepo==null) {count=10.toString}
            else count=topRepo.toString

            val list=MySQLOperationAPIs.topRepo(count)

            if(list.size==1)
            {complete("Sorry not results found!")}
            else {

              var output=""
              val head=list(0)
              output+="[\n"
              list.remove(0)
              var i:Int=0
              for(item<-list)
              {
                i+=1

                output+="  {\n"
                var j:Int=0
                for(element<-item)
                {

                  output+="    \""+ head(j)+"\""
                  j+=1
                  output+=" : "
                  output+="\""+element.toString+"\""
                  if((j!=head.size)){output+=",\n"}
                }

                output+="\n  }"
                if(i!=list.size) output+=",\n"
              }
              output+="\n]"

              complete(output)
            }

            object Route4 {
              val route =
                parameters('topUsers, 'sortBy ? "publicReposCount") { (topUsers,sortBy) =>
                  var count=""
                  if(topUsers==null) {count=10.toString}
                  else count=topUsers.toString


                  val list=MySQLOperationAPIs.topUsers(count, sortBy)
                  if(list.size==1)
                  {complete("Sorry not results found!")}
                  else {

                    var output=""
                    val head=list(0)
                    output+="[\n"
                    list.remove(0)
                    var i:Int=0
                    for(item<-list)
                    {
                      i+=1

                      output+="  {\n"
                      var j:Int=0
                      for(element<-item)
                      {

                        output+="    \""+ head(j)+"\""
                        j+=1
                        output+=" : "
                        output+="\""+element.toString+"\""
                        if((j!=head.size)){output+=",\n"}
                      }

                      output+="\n  }"
                      if(i!=list.size) output+=",\n"
                    }
                    output+="\n]"

                    complete(output)
                  }




                  //            complete(count)

                }}



            //            complete(count)

          }}



      //specify different handlers(called routes here) for our web service
      object MainRouter {
        val routes = Route2.route ~ Route3.route ~ Route1.route
      }

      //It starts an HTTP Server on 104.198.51.240 and port 8080 and replies to GET requests using routes/handler specified
      val bindingFuture = Http().bindAndHandle(MainRouter.routes, "0.0.0.0", 8080)

      println(s"Web Server online at http://localhost:8080/\nPress RETURN to stop...")
      StdIn.readLine() // let it run until user presses return
      bindingFuture
        .flatMap(_.unbind()) // trigger unbinding from the port
        .onComplete(_ => system.terminate()) // and shutdown when done


    }
  }