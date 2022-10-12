package Plugins.CommonUtils.Senders

import Plugins.CommonUtils.Exceptions.HostnameNotFoundException
import Plugins.CommonUtils.ServiceDiscovery.ServiceDiscoveryCommunicateImpls.ServiceDiscoveryCommunicateImpl
import Plugins.CommonUtils.ServiceDiscovery.{ServiceDiscoveryCommunicateMode, ServiceDiscoveryHostnameInfoMode}
import Plugins.CommonUtils.TypedSystem.API.API
import Plugins.CommonUtils.Types.ReplyMessage
import Plugins.CommonUtils.Utils.IOUtils
import Plugins.CommonUtils.Utils.IOUtils.{replyToResult, serialize}
import cats.effect.{IO, Resource}
import io.circe.Decoder
import org.http4s.client.Client
import org.http4s.circe._
import org.http4s.client.dsl.io._
import io.circe.literal._
import org.http4s.Method.POST
import org.http4s._
import io.circe.generic.auto._
import org.http4s.LiteralSyntaxMacros.uri
import org.http4s.implicits.http4sLiteralsSyntax

import scala.reflect.runtime.universe.{TypeTag, typeOf}
import scala.language.higherKinds
import scala.reflect.ClassTag
import scala.reflect.runtime.universe

trait APISender[A <: API] {
  def sendAndGet[B <: A](a : B)(implicit d :Decoder[B#ReturnType]) : IO[B#ReturnType]
  def sendAndGetType[T](a : A)(implicit d :Decoder[T]) : IO[T]
}

object APISender {

  //implicit val replyDecoder: EntityDecoder[IO, ReplyMessage] = jsonOf[IO, ReplyMessage]

  case class HttpAPISender[A <: API]()(implicit client : Client[IO],
                                       discoveryImpl : ServiceDiscoveryCommunicateImpl[ServiceDiscoveryCommunicateMode, ServiceDiscoveryHostnameInfoMode, String, String]
  ) extends APISender[A] {

    override def sendAndGet[B <: A](a : B)(implicit d :Decoder[B#ReturnType]): IO[B#ReturnType] = sendAndGetType[B#ReturnType](a)

    override def sendAndGetType[T](a: A)(implicit d :Decoder[T]): IO[T] = {
        for {
          fwd <- discoveryImpl.getInfo(a.targetServiceCode)
          uri <- IO(Uri.fromString(fwd).getOrElse(throw HostnameNotFoundException()))
          ret <- client.expect(a.makeRequest(uri))(jsonOf[IO, T])
        } yield ret
    }
  }
}
