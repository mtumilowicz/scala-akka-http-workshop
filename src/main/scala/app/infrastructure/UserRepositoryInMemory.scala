package app.infrastructure

import app.domain.{User, UserId, UserRepository, Users}

class UserRepositoryInMemory extends UserRepository {

  private def database = UserDatabase

  def findAll: Users = database.findAll

  def findById(id: String): Option[User] = database.findById(id)

  def save(user: User): User = database.save(user)

  def deleteById(id: String): Option[UserId] = database.deleteById(id)

}
