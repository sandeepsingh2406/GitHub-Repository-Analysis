import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.io.StdIn
import grizzled.slf4j.Logger


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

    //Initiate a logger
    val logger = Logger("WebService Class")

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

              //output for homepage of web service
              logger.info("Request to web service homepage")
              complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Welcome to the Web Service</h1><br><br>Examples: <br><br>" +
                "<u><strong>Note: If web service is created locally, use localhost in all urls, else use google cloud external ip when hosted in google cloud</u></strong><br><br>"+
                "<a href=\"http://localhost:8080/?topUsers=5&sortBy=followersCount\">	http://localhost:8080/?topUsers=5&sortBy=followersCount</a><br><br>" +
                "<a href=\"http://localhost:8080/?topUsers=5&sortBy=followingCount\">	http://localhost:8080/?topUsers=5&sortBy=followingCount</a><br><br>" +
                "<a href=\"http://localhost:8080/?topUsers=5&sortBy=publicReposCount\">	http://localhost:8080/?topUsers=5&sortBy=publicReposCount</a><br><br>" +
                "<a href=\"http://localhost:8080/?topUsers=5&sortBy=subscriptionsCount\">	http://localhost:8080/?topUsers=5&sortBy=subscriptionsCount</a><br><br>" +
                "<a href=\"http://localhost:8080/?topRepo=10\">	http://localhost:8080/?topRepo=10</a><br><br>" +
                "<a href=\"http://localhost:8080/avgLocPerLanguage\">	http://localhost:8080/avgLocPerLanguage</a><br><br>" +
                "<a href=\"http://localhost:8080/?topLanguages=5\">	http://localhost:8080/?topLanguages=5</a><br><br>"+
                "<u><strong>Examples for Repo Recommendation:</strong></u><br><br> "
                +"<a href=\"http://localhost:8080/?getRecommendation=sprintnba\">	http://localhost:8080/?getRecommendation=sprintnba</a><br><br>"
                +"<a href=\"http://localhost:8080/?getRecommendation=deep_recommend_system\">	http://localhost:8080/?getRecommendation=deep_recommend_system</a><br><br>"

                +"<a href=\"http://localhost:8080/?getRecommendation=emoji-mart\">	http://localhost:8080/?getRecommendation=emoji-mart</a><br><br>"


              ))
            }
          }
        }
    }

    //route for seeing top users in github
    object Route2 {
      val route =
        parameters('topUsers, 'sortBy ? "publicReposCount") { (topUsers,sortBy) =>
          var count=""
          if(topUsers==null) {count=10.toString}
          else count=topUsers.toString

          logger.info("Request to see topUsers: "+topUsers+" sortedBy: "+sortBy)

          logger.info("Fetching results from MySQL")
          val list=MySQLOperationAPIs.topUsers(count, sortBy)
          logger.info("Displaying results to user")
          if(list.size==1)
          {complete("Sorry no results found!")

          }

          //if results are there then we display them in json format
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

              output += "    \"" + "url" + "\" : "+"\""+"https://github.com/"+item(0)+"\",\n"

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

    //route for seeing top repos
    object Route3 {
      val route =
        parameters('topRepo) { (topRepo) =>
          var count = ""
          if (topRepo == null) {
            count = 10.toString
          }
          else count = topRepo.toString

          logger.info("Request to see topRepo: count: "+topRepo)
          logger.info("Fetching Results and displaying to user")
          val list = MySQLOperationAPIs.topRepo(count)

          if (list.size == 1) {
            complete("Sorry no results found!")
          }

          //if results are there then we display them in json format

          else {

            var output = ""
            val head = list(0)
            output += "[\n"
            list.remove(0)
            var i: Int = 0
            for (item <- list) {
              i += 1

              output += "  {\n"
              output += "    \"" + "url" + "\" : "+"\""+"https://github.com/"+item(2)+"/"+item(0)+"\",\n"

              var j: Int = 0
              for (element <- item) {

                output += "    \"" + head(j) + "\""
                j += 1
                output += " : "
                output += "\"" + element.toString + "\""
                if ((j != head.size)) {
                  output += ",\n"
                }
              }

              output += "\n  }"
              if (i != list.size) output += ",\n"
            }
            output += "\n]"

            complete(output)
          }
        }
    }


    //route to see top languages as count specified by user
    object Route4 {
      val route =
        parameters('topLanguages ) { (topLanguages) =>
          var count = ""
          if (topLanguages == null) {
            count = 10.toString
          }
          else count = topLanguages.toString

          logger.info("Request to see topLanguages: count: "+topLanguages)


          var map = scala.collection.mutable.Map[String, Int]()

          //fetch results for all languages, then only display the count specified
          logger.info("Fetching results and displaying to user")
          val languages = List("java", "python", "go", "php", "scala", "c", "html", "cpp", "javascript", "csharp")
          for (language <- languages) {

            map += (language -> MongoDBOperationAPIs.getCollectionCount(language + "Collection"))

          }

          var list = map.toList sortBy {_._2}

          list=list.reverse
          list=list.take(count.toInt)


          var output = ""
          val head = ("language","totalRepositories")


          output += "[\n"

          var i: Int = 0
          for (item <- list) {
            i += 1

            output += "  {\n"

            output += "    \"" + head._1 + "\""

            output += " : "
            output += "\"" + item._1 + "\""

            output += ",\n"



            output += "    \"" + head._2 + "\""

            output += " : "
            output += "\"" + item._2 + "\""



            output += "\n  }"
            if (i != list.size) output += ",\n"
          }
          output += "\n]"

          complete(output)



        }
    }

    //route to show average lines of code per language
    object Route5 {
      val route =
        path("avgLocPerLanguage") {
          akka.http.scaladsl.server.Directives.get {
            akka.http.scaladsl.server.Directives.get {

              logger.info("Request to see avgLocPerLanguage:")


              val list = MySQLOperationAPIs.avgLocPerLanguage()


              logger.info("Fetching results and displaying to user")


              if (list.size == 1) {
                complete("Sorry no results found!")
              }

              //if results are there then we display them in json format

              else {

                var output = ""
                val head = list(0)
                output += "[\n"
                list.remove(0)
                var i: Int = 0
                for (item <- list) {
                  i += 1

                  output += "  {\n"
                  var j: Int = 0
                  for (element <- item) {

                    output += "    \"" + head(j) + "\""
                    j += 1
                    output += " : "
                    output += "\"" + element.toString + "\""
                    if ((j != head.size)) {
                      output += ",\n"
                    }
                  }

                  output += "\n  }"
                  if (i != list.size) output += ",\n"
                }
                output += "\n]"

                complete(output)


              }
            }
          }
        }
    }


    //final route for giving recoomendation of similar repo to user on basis of repo entered
    object Route6 {
      val route =
        parameters('getRecommendation, 'count ? "5") { (getRecommendation, count) =>
          akka.http.scaladsl.server.Directives.get {
            akka.http.scaladsl.server.Directives.get {

              logger.info("Request to getRecommendation:" + getRecommendation)


              logger.info("Fetching results from MySQL")
              val list = MySQLOperationAPIs.getSimilarRepo(getRecommendation, count)
              logger.info("Displaying results to user")
              if (list.size == 1) {
                complete("Sorry no results found!")

              }

              //if results are there then we display them in json format

              else {

                var output = ""
                val head = list(0)
                output += "[\n"
                list.remove(0)
                var i: Int = 0
                for (item <- list) {
                  i += 1



                  output += "  {\n"

                  output += "    \"" + "url" + "\" : "+"\""+"https://github.com/"+item(1)+"/"+item(0)+"\",\n"

                  var j: Int = 0
                  for (element <- item) {

                    output += "    \"" + head(j) + "\""
                    j += 1
                    output += " : "
                    output += "\"" + element.toString + "\""
                    if ((j != head.size)) {
                      output += ",\n"
                    }
                  }

                  output += "\n  }"
                  if (i != list.size) output += ",\n"
                }
                output += "\n]"

                complete(output)
              }





            }
          }
        }
    }







    //specify different handlers(called routes here) for our web service
    object MainRouter {
      val routes = Route2.route ~ Route3.route ~  Route4.route  ~  Route5.route ~ Route6.route ~ Route1.route
    }

    logger.info("Initialing web service")
    //It starts an HTTP Server on 104.198.51.240 and port 8080 and replies to GET requests using routes/handler specified
    val bindingFuture = Http().bindAndHandle(MainRouter.routes, "0.0.0.0", 8080)

    println(s"Web Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done

    logger.info("Web Service stopped")


  }
}