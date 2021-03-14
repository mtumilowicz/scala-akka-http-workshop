package app.domain.user

import app.domain.cash.NonNegativeAmount
import app.domain.error.DomainError

class UserService(repository: UserRepository) {

  def incomingAmount(userId: UserId, amount: NonNegativeAmount): Either[DomainError, UserId] =
    repository.findById(userId)
      .map(_.indexIncomingAmount(amount))
      .map(repository.save)
      .map(_.id)

  def outgoingAmount(userId: UserId, amount: NonNegativeAmount): Either[DomainError, UserId] =
    repository.findById(userId)
      .flatMap(_.indexOutgoingAmount(amount))
      .map(repository.save)
      .map(_.id)

  def findAll(): Users = repository.findAll

  def save(input: NewUserInput): User = repository.save(input)

  def replace(input: ReplaceUserInput): Either[DomainError, User] = {
    val user = User.createFrom(input)
    repository.findById(user.id)
      .map(_ => user)
      .map(repository.save)
  }

  def findById(id: UserId): Either[DomainError, User] = repository.findById(id)

  def deleteById(id: UserId): Option[UserId] = repository.deleteById(id)
}