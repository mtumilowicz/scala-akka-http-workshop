package app.domain.user

import app.domain.error.DomainError

trait UserRepository {

  def findAll: Users

  def findById(id: UserId): Either[DomainError, User]

  def save(input: NewUserInput): User

  def save(user: User): User

  def deleteById(id: UserId): Option[UserId]
}
