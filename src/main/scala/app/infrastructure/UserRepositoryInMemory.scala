package app.infrastructure

import app.domain.{User, UserRepository, Users}

class UserRepositoryInMemory extends UserRepository {

  private def database: UserDatabase.type = UserDatabase

  def findAll: Users = database.findAll

  def findById(id: String): Option[User] = database.findById(id)

  def save(user: User): Unit = database.save(user)

  def delete(userName: String): Unit = database.delete(userName)

}
