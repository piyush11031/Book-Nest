import { Component } from '@angular/core';
import {AuthenticationRequest} from "../../services/models/authentication-request";
import {Router} from "@angular/router";
import {AuthenticationService} from "../../services/services/authentication.service";
import {TokenService} from "../../services/token/token.service";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  //initialize an AuthenticationRequest
  authRequest: AuthenticationRequest = {email: '', password: ''};

  errorMsg: Array<string> = []; //contains errors returned from backend

  constructor(
    private router: Router,
    private authService: AuthenticationService,
    private tokenService: TokenService
  ) {
  }

  login() {
    this.errorMsg = []; //this deletes the old error messages from previous login attempts
    this.authService.authenticate({
      body: this.authRequest
    }).subscribe({
      next: (res) => {
        this.tokenService.token = res.token as string
        this.router.navigate(['books']);
      },
      error: (err) => {
        console.log(err);
        if(err.error.validationError){
          this.errorMsg = err.error.validationErrors;
        }
        else{
          this.errorMsg.push(err.error.error)
        }
      }
    })
  }

  register() {
    this.router.navigate(['register']) //navigate user to register page
  }
}
