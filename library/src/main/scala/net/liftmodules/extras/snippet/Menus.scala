package net.liftmodules.extras
package snippet

import scala.xml._

import net.liftweb._
import common._
import http.{LiftRules, Req, S}
import sitemap.{Loc, SiteMap}
import util._
import util.Helpers._

// <a data-lift="Menus.item?name=Register"><i class="icon-edit"></i> <span data-lift-name="link-display">Register</span></a>

object Menus extends SnippetExtras {
  // Is this necessary?
  // TODO: Add a place to put the text
  def item(html: NodeSeq): NodeSeq = {
    (for {
      name <- S.attr("name")
      loc <- SiteMap.findAndTestLoc(name)
      link <- loc.createDefaultLink
    } yield {
      "a [href]" #> link
    }).map(_.apply(html))
  }

  // For use with bootstrap
  def group = {
    val menus: NodeSeq =
      for {
        group <- S.attr("group") ?~ "Group not specified"
        sitemap <- LiftRules.siteMap ?~ "Sitemap is empty"
        request <- S.request ?~ "Request is empty"
        curLoc <- request.location ?~ "Current location is empty"
      } yield ({
        val currentClass = S.attr("current_class").openOr("active")
        sitemap.locForGroup(group) flatMap { loc =>
          val nonHiddenKids = loc.menu.kids.filterNot(_.loc.hidden)
          val styles =
            if (curLoc.name == loc.name || loc.menu.kids.exists(_.loc.name == curLoc.name)) currentClass
            else ""

          if (nonHiddenKids.length == 0) {
            <li class={styles}>{SiteMap.buildLink(loc.name)}</li>
          }
          else {
            val dropdown: NodeSeq = nonHiddenKids.map { kid =>
              <li>{SiteMap.buildLink(kid.loc.name)}</li>
            }

            <li class={styles + " dropdown"}>
              <a href="#" class="dropdown-toggle" data-toggle="dropdown">{loc.linkText.openOr(Text("Empty Name"))} <b class="caret"></b></a>
              <ul class="dropdown-menu">{ dropdown }</ul>
            </li>
          }
        }
      }): NodeSeq

    "* *" #> menus
  }
}
