/**
  * Created by avp on 12/1/2016.
  *
  * ToDo
  * close connection client
  */
import java.sql.{Connection, DriverManager}

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException

object MySQLOperationAPIs {

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
  }

  def main(args: Array[String]): Unit ={
//    println(insertTopRepoCommitsTable("sdsdgf", 123, 123, 245));
//    println(insertTopRepoLanguageTable("Adfasdg", 324545456, "java", 1232454, 12));
//    println(insertAllLanguageRepoTable("bash_like_shellafa", "6135180730".toLong, "captainriku75", "19618265".toLong, "2016-06-15", "2016-06-15",
//      1, 2, 3, 100));
//    println(insertUserTable("dg234dfag", 123556, 35, 78, 71, 7));
//    testDBConnection();
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
        println("Exception in insertTopRepoCommitsTable()");
      }
    }
    return false;
  }


  def testDBConnection(): Unit = {
    val statement = connection.createStatement;
    val resultSet = statement.executeQuery("show tables;");
    println("Fetch successful");
    while (resultSet.next) {
      println(resultSet.getString("Tables_in_mysql"));
    }
  }
}
