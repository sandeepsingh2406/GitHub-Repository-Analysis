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
  var connection:Connection = DriverManager.getConnection(url, ParameterConstants.mysqlUserName, ParameterConstants.mysqlPassword);
  val statement = connection.createStatement;

  def main(args: Array[String]): Unit ={
    println("hello");
  }

  def testDBConnection(): Unit = {
    val resultSet = statement.executeQuery("show tables;");
    while (resultSet.next) {
      println(resultSet.getString("Tables_in_cs441project"));
    }
  }
}
