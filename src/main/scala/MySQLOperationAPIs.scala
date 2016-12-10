/**
  * Created by avp on 12/1/2016.
  *
  * ToDo
  * close connection client
  */
import java.sql.{Connection, DriverManager}

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import grizzled.slf4j.Logger

import scala.collection.mutable.ListBuffer

object MySQLOperationAPIs {

  val logger = Logger("MySQLOperationAPIs")


  //  val url = "jdbc:mysql://localhost:8889/mysql";
  val url = ParameterConstants.mysqlPrefix + "://" + ParameterConstants.mysqlHostIPAddress +
    ":" + ParameterConstants.mysqlDBPortNumber + "/" + ParameterConstants.mysqlDBName;
  //      ":" + ParameterConstants.mysqlDBPortNumber + "/" + ParameterConstants.mysqlDBName;
  //      "/" + ParameterConstants.mysqlDBName;

  var connection:Connection = _;
  try{
    Class.forName(ParameterConstants.mysqlDriver);
    connection = DriverManager.getConnection(url, ParameterConstants.mysqlUserName, ParameterConstants.mysqlPassword);
    println("Connection successful.");
  } catch {
    case e:Throwable => println("Exception in getConnection()");
      logger.error("Exception in getConnection(): "+e.getMessage)

  }



  def main(args: Array[String]): Unit ={
    //    println(insertTopRepoCommitsTable("sdsdgf", 123, 123, 245));
    //    println(insertTopRepoLanguageTable("Adfasdg", 324545456, "java", 1232454, 12));
    //    println(insertAllLanguageRepoTable("bash_like_shellafa", "6135180730".toLong, "captainriku75", "19618265".toLong, "2016-06-15", "2016-06-15",
    //      1, 2, 3, 100));
    //    println(insertUserTable("dg234dfag", 123556, 35, 78, 71, 7));
    testDBConnection();
    //    print(avgLocPerLanguage().foreach(item=>item.foreach(element=>println(element+" "))))
    //    print(getSimilarRepo("SprintNBA","5"))

  }

  // insert into userTable
  def insertUserTable(userName:String, userID:Long, publicReposCount:Int, followersCount:Int, followingCount:Int,
                      subscriptionsCount:Int): Int = {
    var result = -1;
    val query = "INSERT INTO `usertable`(`userName`, `userID`, `publicReposCount`, `followersCount`, `followingCount`, " +
      "`subscriptionsCount`) VALUES (\"" + userName + "\", " + userID + ", " + publicReposCount + ", " +
      followersCount + ", " + followingCount + ", " + subscriptionsCount + ");";
    try {
      val statement = connection.createStatement();
      result = statement.executeUpdate(query);
    } catch {
      case integrityException:MySQLIntegrityConstraintViolationException => {
        println("Duplicate key found for user: " + userName);

      }
      case e:Throwable => {
        println("Exception in insertUserTable()" );
        e.printStackTrace();
        logger.error("Exception in insertUserTable(): "+e.getMessage)

      }
    }

    return result;
  }

  // insert into allLanguageRepoTable
  def insertAllLanguageRepoTable(repoName:String, repoID:Long, ownerUserName:String, ownerID:Long, createdAt:String,
                                 updatedAt:String, watchersCount:Int, forksCount:Int, openIssues:Int, repoSize:Int): Int = {
    var result = -1;
    val query = "INSERT INTO `alllanguagerepotable`(`repoName`, `repoID`, `ownerUserName`, `ownerID`, `createdAt`, " +
      "`updatedAt`, `watchersCount`, `forksCount`, `openIssue`, `repoSize`) VALUES (\"" + repoName + "\", " + repoID + ", \""+
      ownerUserName + "\", " + ownerID + ", \"" + createdAt + "\", \"" + updatedAt + "\", " + watchersCount + ", " + forksCount +
      ", " + openIssues + ", " + repoSize + ");";
    try {
      val statement = connection.createStatement();
      result = statement.executeUpdate(query);
    } catch {
      case integrityException:MySQLIntegrityConstraintViolationException => {
        println("Duplicate key found for repo: " + repoName);
      }
      case e:Throwable => {
        println("Exception in insertAllLanguageRepoTable()" );
        e.printStackTrace();
        logger.error("Exception in insertAllLanguageRepoTable(): "+e.getMessage)


      }
    }
    /*
    * INSERT INTO `alllanguagerepotable`(`repoName`, `repoID`, `ownerUserName`, `ownerID`, `createdAt`, `updatedAt`, `watchersCount`, `forksCount`, `subscibersCount`) VALUES ([value-1],[value-2],[value-3],[value-4],[value-5],[value-6],[value-7],[value-8],[value-9])
    * */

    return result;
  }

  // insert query for topRepoLanguageTable
  def insertTopRepoLanguageTable(repoName:String, repoID:Long, language:String, numberOfLines:Long, numberOfFiles:Int): Int = {
    var result = -1;
    val query = "INSERT INTO `toprepolanguagetable`(`repoName`, `repoID`, `language`, `numberOfLines`, `numberOfFiles`) VALUES (\"" +
      repoName + "\", " + repoID + ", \"" + language.toLowerCase() + "\", " + numberOfLines + ", " + numberOfFiles + ");";
    try {
      val statement = connection.createStatement();
      result = statement.executeUpdate(query);
    } catch {
      case integrityException:MySQLIntegrityConstraintViolationException => {
        println("Duplicate key found for repo: " + repoName);
      }
      case e:Throwable => {
        println("Exception in insertTopRepoLanguageTable()" );
        e.printStackTrace();

      }
    }
    return result;
  }

  // insert query for topRepoCommitsTable
  def insertTopRepoCommitsTable(repoName:String, repoID:Long, numberOfCommits:Int, numberOfFiles:Int): Int = {
    val query = "INSERT INTO `toprepocommitstable`(`repoName`, `repoID`, `numberOfCommits`, `numberOfFiles`) VALUES (\"" +
      repoName + "\", " + repoID + ", " + numberOfCommits + ", " + numberOfFiles + ");";
    var result = -1;
    try {
      val statement = connection.createStatement();
      result = statement.executeUpdate(query);
    } catch {
      case integrityException:MySQLIntegrityConstraintViolationException => {
        println("Duplicate key found for repo: " + repoName);
      }
      case e:Throwable => {
        println("Exception in insertTopRepoCommitsTable()");
        e.printStackTrace();
      }
    }
    return result;
  }

  //check if row exists in table

  def checkRow(repoName:String): Boolean = {
    val query = "SELECT * FROM `toprepocommitstable` WHERE repoName=\""+repoName+"\"";
    var result = -1;
    try {
      val statement = connection.createStatement();
      val rs = statement.executeQuery(query)
      if(rs.next()){
        return true
      }
      else
        return false
    } catch {
      case e:Throwable => {
        println("Exception in checkRow()"+e);
      }
    }
    return false;
  }


  //get top users according to input criteria via argument
  def topUsers(count:String,sortBy:String):  ListBuffer[ListBuffer[String]] = {
    val query = "select * from `usertable` order by "+sortBy+" desc limit "+count
    println(query)
    var results=new ListBuffer[ListBuffer[String]]();
    try {
      val statement = connection.createStatement();
      val rs = statement.executeQuery(query)

      results+=ListBuffer[String]("userName", "userID", "publicReposCount", "followersCount", "followingCount", "subscriptionsCount")
      while(rs.next()){

        var templist=ListBuffer[String]()
        for(j<-1 until 7)
        {templist+=rs.getString(j)}

        results += templist
      }


    } catch {
      case e:Throwable => {
        println("Exception in topUsers()"+e);
      }
    }
    return results;
  }

  //get top repositories sorted by popularity, which a function of watchers and forks count
  def topRepo(count:String):  ListBuffer[ListBuffer[String]] = {
    val query = "  select * from alllanguagerepotable order by(watchersCount+forksCount) desc limit "+count
    var results=new ListBuffer[ListBuffer[String]]();
    try {
      val statement = connection.createStatement();
      val rs = statement.executeQuery(query)

      results+=ListBuffer[String]("repoName",
        "repoID",
        "ownerUserName",
        "ownerID",
        "createdAt",
        "updatedAt",
        "watchersCount",
        "forksCount",
        "openIssue",
        "repoSize")
      while(rs.next()){

        var templist=ListBuffer[String]()
        for(j<-1 until 11)
        {templist+=rs.getString(j)}

        results += templist
      }


    } catch {
      case e:Throwable => {
        println("Exception in topRepo()"+e);
      }
    }
    return results;
  }

  //get average lines of code of all languages we have data for in mysql
  def avgLocPerLanguage():  ListBuffer[ListBuffer[String]] = {
    val query = " select language,sum(numberOfLines)/sum(numberOfFiles) as average_loc from toprepolanguagetable "+
      "group by language order by average_loc desc"
    var results=new ListBuffer[ListBuffer[String]]();
    try {
      val statement = connection.createStatement();
      val rs = statement.executeQuery(query)

      results+=ListBuffer[String]("language","average_loc")
      while(rs.next()){

        var templist=ListBuffer[String]()
        for(j<-1 until 3)
        {templist+=rs.getString(j)}

        results += templist
      }


    } catch {
      case e:Throwable => {
        println("Exception in avgLocPerLanguage()"+e);
        logger.info("Exception in avgLocPerLanguage()"+e.getMessage)
      }
    }
    return results;
  }

  //user enters a repo, we find similar repo to it, so we can recommend them to the user. similarity is found by parameters language, popularity and size
  def getSimilarRepo(repoName:String, count:String): ListBuffer[ListBuffer[String]] ={

    val query = " select distinct(t.repoName),a.ownerUserName" +
      " from toprepolanguagetable t, alllanguagerepotable a," +
      "(select top.repoName as repoName,top.language as language,(ar.forksCount + ar.watchersCount) as popularity, ar.repoSize as repoSize " +
      "from toprepolanguagetable top, alllanguagerepotable as ar " +
      "where top.repoName = ar.repoName and ar.repoName=\""+repoName+"\") userRepo " +
      "where t.repoName=a.repoName " +
      "and t.repoName!=userRepo.repoName " +
      "and userRepo.language=t.language " +
      "and a.repoSize>= 0.75*userRepo.repoSize " +
      "and a.repoSize<= 1.25*userRepo.repoSize " +
      "and (a.forksCount + a.watchersCount)>= 0.75*userRepo.popularity " +
      "and (a.forksCount + a.watchersCount)<= 1.25*userRepo.popularity " +
      "limit "+count


    var results=new ListBuffer[ListBuffer[String]]();
    try {
      val statement = connection.createStatement();
      val rs = statement.executeQuery(query)

      results+=ListBuffer[String]("repoName","ownerUserName")
      while(rs.next()){

        var templist=ListBuffer[String]()
        for(j<-1 until 3)
        {templist+=rs.getString(j)}

        results += templist
      }


    } catch {
      case e:Throwable => {
        println("Exception in getSimilarRepo()"+e);
        logger.info("Exception in getSimilarRepo()"+e.getMessage)

      }
    }
    return results;





  }



  def testDBConnection(): Unit = {
    val statement = connection.createStatement;
    val resultSet = statement.executeQuery("show tables;");
    println("Fetch successful");
    while (resultSet.next) {
      println(resultSet.getString("Tables_in_" + ParameterConstants.usageDBName));
    }
  }
}
