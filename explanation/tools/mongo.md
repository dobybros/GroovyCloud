### 1.@Database
@Database指定在类上，通过name参数指定数据库名。
```
@Database(name = "project")
class ProjectDatabase {

}
```
此事例指定数据库名为Project。
### 2.@DBDocument
DBDocument指定在类上，标明此类是一个实体类，是可以与Mongodb中的表进行关系映射,使得对象属性与表字段对应。DBDocument注解需要指定collectionClass属性，以此指明该实体映射的collection所在数据库。实体类需要继承DataObject类，DataObject类含有id属性(数据记录的唯一标识)。
```
@DBDocument(database = ProjectDataBase.class)
class User extends DataObject{
     @DocumentField(key = "username")
     private String userName
     @DocumentField(key = "age")
     private Integere age
     @DocumentField(key = "sex")
     private String sex
     
     Document toDocument() {
         Document document = new CleanDocument()
         document.append("username", username)
                 .append("age", age)
                 .append("sex", sex)
        return document
     }
     
     getter 
     .....
     setter
     .....
}

```
以上创建了一个User实体类，并且映射到project数据库下，映射字段为username, age, sex。
### 3.@DBCollection
DBCollection指定在类上，指明是Dao层，对数据库操作。还需要在类上指定@Bean注解，这样才能在其它类中被注入。
> sample
```
@DBCollection(name = "user", database = Project.class)
@Bean
class UserCollection extends MongoCollectionHelper{
    def addUser(User user){
       MongoCollection<User> mongoCollection = getMongoCollection()
       mongoCollection.insertOne(user)
    }
    def updateUser(String id, User user){
        MongoCollection<User> mongoCollection = getMongoCollection()
        Document query = new Document(User.FIELD_ID, id)
        mongoCollection.updateOne(query, new Document("\$set", user.toDocument), new UpdateOption().upsert(false))
    }
    def deleteUser(String id){
        MongoCollection<User> mongoCollection = getMongoCollection()
        Document query = new Document(User.FIELD_ID, id)
        mongoCollection.deleteOne(query)
    }
    def findUser(String userName) {
       MongoCollection<User> mongoCollection = getMongoCollection()
       Document query = new Document("username", userName)
       FindIterable<TransactionInfo> iterable = mongoCollection.find(query)
       MongoCursor<TransactionInfo> cursor = iterable.iterator()
       User user = null
       while(cursor.hasNext())
            user = cursor.next()
       return user
    }
}
```

属性name指定操作的实体需要映射到的表名，database指定映射的collection所在数据库。当通过addUser向数据库插入一条记录后，发现mongodb下多出了project数据库，数据库下多了一个名为user 的 collection。Dao层必须继承 MongoCollectionHelper， 这样可以方便的获得MongoCollection对象，用来操作数据库。
### 4.@DocuemntField
DocuemntField注解加在属性上，表明对象属性与表字段之间的映射关系。DocuemntField的 key属性指明映射到表中的字段名。
