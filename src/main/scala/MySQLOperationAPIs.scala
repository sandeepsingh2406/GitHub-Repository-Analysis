/**
  * Created by avp on 12/1/2016.
  *
  * ToDo
  * close connection client
  */

import java.beans.Statement
import java.sql.{Connection, DriverManager}

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
  } catch {
    case e => println("Exception in insertTopRepoCommitsTable()");
  }

  def main(args: Array[String]): Unit ={
    println(insertTopRepoCommitsTable("sdsdgf", 123, 123, 245));
    println(insertTopRepoLanguageTable("Adfasdg", 324545456, "java", 1232454, 12));
  }

  def insertTopRepoLanguageTable(repoName:String, repoID:Long, language:String, numberOfLines:Long, numberOfFiles:Int): Int = {
//    INSERT INTO `toprepolanguagetable`(`repoName`, `repoID`, `languge`, `numberOfLines`, `numberOfFiles`) VALUES ([value-1],[value-2],[value-3],[value-4],[value-5])
    var result = -1;
    val query = "INSERT INTO `toprepolanguagetable`(`repoName`, `repoID`, `language`, `numberOfLines`, `numberOfFiles`) VALUES (\"" +
    repoName + "\", " + repoID + ", \"" + language.toLowerCase() + "\", " + numberOfLines + ", " + numberOfFiles + ");";
    try {
      val statement = connection.createStatement();
      result = statement.executeUpdate(query);
    } catch {
      case e => {
        println("Exception in insertTopRepoLanguageTable()" );
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
      case e => println("Exception in insertTopRepoCommitsTable()");
    }
    return result;
  }

  def testDBConnection(): Unit = {
    val statement = connection.createStatement;
    val resultSet = statement.executeQuery("show tables;");
    while (resultSet.next) {
      println(resultSet.getString("Tables_in_cs441project"));
    }
  }
}
