Anyframe Java Core, release 5.5.0 (2012.12)
------------------------------------------------
http://www.anyframejava.org

1. Anyframe Java Core(Enterprise Ed.) 소개

Anyframe Java Core(Enterprise Ed.)는 Spring 기반에서 다양한 best-of-breed 오픈 소스를 통합 및 확장하여 구성한 어플리케이션 프레임워크, MVC 아키텍처를 준수하여 웹 어플리케이션의 프리젠테이션 레이어를 구조적으로 개발할 수 있도록 지원하는 웹 프레임워크를 제공하며 프레임워크 기반의 업무용 프로그램 개발을 효과적으로 진행할 수 있도록 기술 공통 서비스, 템플릿 기반의 프로젝트 구조 및 샘플 코드, 매뉴얼 등을 제공한다.	 또한 Eclipse Plugin 형태의 Anyframe Java IDE를 제공함으로써 기존 방식보다 훨씬 쉽고 빠르게 Anyframe 기반 개발 환경을 구성할 수 있도록 Scaffolding, 코드 생성 기능 등을 지원한다.

Anyframe Java Core(Enterprise Ed.) 관련 문서는 포탈 사이트의 문서 메뉴 내의 매뉴얼을 통해 확인할 수 있다.

* Anyframe Java Core(Enterprise Ed.) 포탈 사이트 : http://www.anyframejava.org

2. Anyframe Java Core(Enterprise Ed.) 프로젝트 빌드 절차

1) Maven 설치 
   - 2.2.1 버전의 Maven을 설치하도록 한다.
   - 다운로드 URL :http://maven.apache.org/download.html

2) settings.xml 설정
   - Maven 설치 폴더/conf/settings.xml 파일에 아래와 같이 <profile>을 추가한 후, <activeProfile>에 등록하도록  
     한다.
   ############################settings.xml############################
   <profiles>
     <profile>
        <id>anyframeprofile</id>
        <repositories> 
           <repository>
              <id>anyframe-repository</id>
              <name>repository for Anyframe</name>                 
              <url>http://dev.anyframejava.org/maven/repo</url>
              <snapshots>
                 <enabled>true</enabled>
              </snapshots>
           </repository>                                    
        </repositories>   
        <pluginRepositories>
           <pluginRepository>
              <id>anyframe-plugin-repository</id> 
              <name>repository for Anyframe Plugins Repository</name>       
              <url>http://dev.anyframejava.org/maven/repo</url>           
           </pluginRepository>
           <pluginRepository>
              <id>central</id> 
              <name>Internal Mirror of Central Plugins Repository</name>       
              <url>http://www.ibiblio.org/maven2/plugins</url>           
           </pluginRepository>
           <pluginRepository>
              <id>remote</id> 
              <name>Internal Mirror of Central Plugins Repository</name>       
              <url>http://repo1.maven.org/maven2</url>           
           </pluginRepository>    
        </pluginRepositories>
     </profile>
   </profiles>
   <activeProfiles>
      <activeProfile>anyframeprofile</activeProfile>
   </activeProfiles>
   ####################################################################
   * Anyframe Java Core Maven Repository 내에 상용 라이브러리가 포함되어 있지 않으므로, 일부 프로젝트에는 컴파일 오류가 존재한다. Anyframe Java Core(Enterprise Ed.) Maven Repository 내에 배포되지 않은 상용 라이브러리는 해당 프로젝트의 pom.xml 파일 내에 정의된 상용 라이브러리의 group id, artifact id, version을 참고하여 개인 Local Respository 내에 배포하도록 한다.

3) 환경변수 PATH에 Maven 설치 폴더/bin을 추가한다.

4) 명령창을 오픈하고, 각 프로젝트의 Root 폴더(pom.xml 파일이 존재하는 폴더)에서 mvn clean compile을 수행함으로써, 필요한 라이브러리를 Maven Repository로부터 다운로드할 수 있도록 한다.

5) 각 프로젝트를 패키징하고자 할 경우, mvn package를 수행하면 해당 프로젝트의 target 폴더 내에 패키징된 라이브러리를 생성시킬 수 있다.

3. Anyframe Java Core Libraries 상세

1) Available Archetypes
   . Basic Archetype : anyframe-basic-archetype-5.2.1.jar
   . Service Archetype : anyframe-service-archetype-2.2.1.jar

2) Available Plugins
   - Essential Plugins
   . Core Plugin : anyframe-core-pi-1.5.0.jar
   . Datasource Plugin : anyframe-datasource-pi-1.0.2.jar
   . Logging Plugin : anyframe-logging-pi-1.0.2.jar
   . Spring Plugin : anyframe-spring-pi-1.0.2.jar
   - Optional Plugins
   . Cache Plugin : anyframe-cache-pi-1.0.4.jar
   . Chart Plugin : anyframe-chart-pi-1.0.3.jar
   . CXF Plugin : anyframe-cxf-pi-1.0.4.jar
   . CXF JAX-RS Plugin : anyframe-cxf-jaxrs-pi-1.0.4.jar
   . CXF JAX-WS Plugin : anyframe-cxf-jaxws-pi-1.0.4.jar
   . Excel Plugin : anyframe-excel-pi-1.0.4.jar
   . FileUpload Plugin : anyframe-fileupload-pi-1.0.3.jar
   . Flex Query Plugin : anyframe-flex-query-pi-1.5.0.jar
   . Generic Plugin : anyframe-generic-pi-1.5.0.jar
   . Hibernate Plugin : anyframe-hibernate-pi-1.5.0.jar
   . IAM Plugin : anyframe-iam-pi-1.0.3.jar
   . I18N Plugin : anyframe-login-pi-1.0.3.jar   
   . iBatis2 Plugin : anyframe-ibatis2-pi-0.0.6.jar 
   . Idgen Plugin : anyframe-idgen-pi-1.5.0.jar
   . JasperReports Plugin : anyframe-jasperreports-pi-1.0.2.jar
   . JdbcSupport Plugin : anyframe-jdbc-support-pi-1.0.3.jar
   . jQuery Plugin : anyframe-jquery-pi-1.0.4.jar
   . Logback Plugk : anyframe-logback-pi-1.0.1.jar
   . Logging Sql Plugin : anyframe-logging-sql-pi-1.0.3.jar
   . Log Manager Plugin : anyframe-logmanager-pi-1.0.1.jar
   . MiP Query Plugin : anyframe-mip-query-pi-1.5.0.jar
   . Monitoring Plugin : anyframe-monitoring-pi-1.0.2.jar
   . Mybatis Plugin : anyframe-mybatis-1.0.1.jar
   . OSCache Plugin : anyframe-oscache-1.5.0.jar
   . Query Plugin : anyframe-query-pi-1.5.0.jar
   . Query Ria Plugin : anyframe-query-ria-pi-1.5.0.jar
   . Remoting Plugin : anyframe-remoting-pi-1.0.4.jar
   . RoutingDataSource Plugin : anyframe-routingdatasource-pi-1.0.3.jar
   . Scheduling Plugin : anyframe-scheduling-pi-1.5.0.jar
   . Simpleweb Plugin : anyframe-simpleweb-pi-1.0.2.jar
   . Simpleweb jQuery Plugin : anyframe-simpleweb-jquery-pi-1.0.3.jar
   . Simpleweb Map Plugin : anyframe-simpleweb-map-pi-1.0.2.jar
   . Simpleweb VO Plugin : anyframe-simpleweb-vo-pi-1.0.2.jar
   . Spring Optional Plugin : anyframe-spring-optional-pi-1.0.2.jar
   . Spring REST Plugin : anyframe-springrest-pi-1.0.3.jar
   . Struts Plugin : anyframe-struts-pi-1.0.3.jar
   . Test Plugin : anyframe-test-pi-1.0.3.jar
   . Tiles Plugin : anyframe-tiles-pi-1.0.3.jar
   . Util Plugin : anyframe-util-pi-1.0.3.jar
   . Util System Plugin : anyframe-util-system-pi-1.5.0.jar
   . Webflow Plugin : anyframe-webflow-pi-1.0.2.jar
   . XP Query Plugin : anyframe-xp-query-pi-1.5.0.jar
   . XPlatform Plugin : anyframe-xplatform-pi-1.5.0.jar
   . Online Plugin : anyframe-online-pi-4.5.0.jar
  
3) Available Services
   . Core : anyframe-core-1.5.0.jar
   . Cache : anyframe-cache-1.0.4.jar
   . Chart : anyframe-chart-1.0.3.jar
   . CXF Plugin : anyframe-cxf-1.0.4.jar
   . Flex Query : anyframe-flex-query-1.5.0.jar
   . Generic : anyframe-generic-1.5.0.jar
   . Hibernate : anyframe-hibernate-1.5.0.jar
   . Idgen : anyframe-idgen-1.5.0.jar
   . JasperReports : anyframe-jasperreports-1.0.2.jar
   . Jdbc Support : anyframe-jdbc-support-1.0.3.jar
   . Log Manager : anyframe-logmanager-1.0.2.jar
   . MiP Query : anyframe-mip-query-1.5.0.jar
   . OSCache : anyframe-oscache-1.5.0.jar
   . Query : anyframe-query-1.5.0.jar
   . Query Ria : anyframe-query-ria-1.5.0.jar
   . Scheduling : anyframe-scheduling-1.5.0.jar
   . Simpleweb : anyframe-simpleweb-1.0.2.jar
   . Struts : anyframe-struts-1.0.3.jar
   . Tiles : anyframe-tiles-1.0.3.jar
   . Util-System : anyframe-util-system-1.5.0.jar
   . XP Query : anyframe-xp-query-1.5.0.jar
   . XPlatform : anyframe-xplatform-1.5.0.jar
   . Online : anyframe-online-4.5.0.jar

4) Available IDE - Commands
   . Common for Command : anyframe-ide-command-common-2.2.1.jar
   . Command for Maven : anyframe-maven-plugin-2.2.1.jar
   . Command Ant CLI : anyframe-ide-command-cli-2.2.1.jar
   . Command for Ant : anyframe-ide-command-ant-2.2.1.jar
   . Aspect for Ant : anyframe-ide-command-aspect-2.2.1.jar

4. 라이센스 정책

Anyframe Java Core(Enterprise Ed.)는 라이센스 정책으로 Apache Licence, Version 2.0 (http://www.apache.org)을 채택한다. 
단, Anyframe Java Core(Enterprise Ed.) 내에서 사용된 외부 오픈 소스의 경우 원 오픈 소스의 라이센스 정책을 따르고, Online Service의 경우 라이센스 정책으로 Anyframe Java Core(Enterprise Ed.)을 채택한다.
Online Service와 관련된 라이센스 정책은 license (Enterprise Edition).txt 파일을 참고하면 된다.
