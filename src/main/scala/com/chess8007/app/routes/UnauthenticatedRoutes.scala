package com.chess8007.app.routes

import cats.effect.IO
import com.chess8007.app.AppConfig
import com.chess8007.app.database.DatabaseResource
import com.chess8007.app.services.{AuthenticationService, UserService}
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.io.*

class UnauthenticatedRoutes(db: DatabaseResource) {
  private val userService = UserService.of(db)

  private def signUpUser(payload: UserSignUpPayload): IO[Either[Throwable, UserSignUpResponse]] = {
    userService.signUpUser(payload).map { userEither => 
      userEither.map( user =>
        UserSignUpResponse(user.userId, user.username)
      )
    }
  }

  def getSignUpRoute: HttpRoutes[IO] = HttpRoutes.of {
    case req @ POST -> Root / "api" / "signUp" =>
      for {
        userSignUpPayload <- req.as[UserSignUpPayload]
        user <- userService.signUpUser(userSignUpPayload)
        response <- user match {
          case Right(user) => Ok(user)
          case Left(error) => InternalServerError(s"Internal Server Error: ${error.toString}")
        }
      } yield response
  }
}

object UnauthenticatedRoutes {
  def of(db: DatabaseResource): UnauthenticatedRoutes = new UnauthenticatedRoutes(db)
}
