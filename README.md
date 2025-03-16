# jaguar

## 関連リンク

* [サンプルコード](https://github.com/wildfly-extras/wildfly-jar-maven-plugin/tree/11.0.2.Final/examples/jsf-ejb-jpa)

* [DatabaseRider](https://database-rider.github.io/database-rider/1.44.0/documentation.html?theme=foundation#_introduction)

* [weld-testing(Junit5)](https://github.com/weld/weld-testing/tree/master/junit5)

* [Transaction管理1](https://in.relation.to/2019/01/23/testing-cdi-beans-and-persistence-layer-under-java-se/)

* [Transaction管理2](https://stackoverflow.com/questions/59776325/weld-and-junit-no-transactionmanager)

## 環境

<pre>
PS C:\Workspaces\jaguar> mvn -v
Apache Maven 3.9.6 (bc0240f3c744dd6b6ec2920b3cd08dcc295161ae)
Maven home: C:\Program Files\Maven\apache-maven-3.9.6
Java version: 17.0.9, vendor: Oracle Corporation, runtime: C:\Program Files\Java\jdk-17
Default locale: ja_JP, platform encoding: MS932
OS name: "windows 11", version: "10.0", arch: "amd64", family: "windows"
</pre>

## 実行手順

1. mvn clean package -P wildfly-bootable-jar
1. java -jar  target/weld-testing-bootable.jar
1. http://127.0.0.1:8080/
