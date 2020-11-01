package answers.app.infrastructure

import answers.app.domain._

class UserInMemoryRegistry(private val map: Map[UserId, User] = Map())
  extends UserRegistry {

  def findAll: Users = Users(map.values.toSeq)

  def findById(id: UserId): Option[User] = map.get(id)

  def save(input: NewUserInput): (User, UserInMemoryRegistry) = {
    val user = User.createFrom(UserId(java.util.UUID.randomUUID().toString), input)

    (user, new UserInMemoryRegistry(map + (user.id -> user)))
  }

  def save(user: User): UserInMemoryRegistry =
    new UserInMemoryRegistry(map + (user.id -> user))

  def deleteById(id: UserId): UserInMemoryRegistry =
    new UserInMemoryRegistry(map - id)
}
