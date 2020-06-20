package edu.csula.rubrics;

import java.io.UnsupportedEncodingException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

@Controller
@RequestMapping("/canvas")
public class CanvasController {
	
	@GetMapping("/hello")
	public String hello(Model model, @RequestParam(value = "name", required = false, defaultValue = "World") String name) {
		model.addAttribute("name", name);
		return "hello";
	}
	
	//return userid if already login
	@GetMapping("/loginStatus")
	public Long loginStatus(@CookieValue("rubric-alice-token") String jwt) {
		//jwt is the token generated by RubricService
		System.out.println("get cookie: "+jwt);
		//call canvas API.
		if(jwt==null||jwt.length()==0)
			return (long)-1;
		
		try {
			Jws<Claims> claims = Jwts.parser()
			  .setSigningKey("secret".getBytes("UTF-8"))
			  .parseClaimsJws(jwt);
			String username = claims.getBody().get("username").toString();
			return Long.parseLong(claims.getBody().getId());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return (long)-1;
		
	}
}