import { Component } from '@angular/core';
import {RegistrationsRequest} from "../../services/models/registrations-request";
import {Router} from "@angular/router";
import {AuthenticationService} from "../../services/services/authentication.service";

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {

  registerRequest: RegistrationsRequest = {email: '', firstName: '', lastName: '', password: ''};
  errorMsg: Array<string> = [];

  constructor(
    private router: Router,
    private authService: AuthenticationService
  ){

  }

  login() {
    this.router.navigate(['login']);
  }

  register() {
    this.errorMsg = [];

    this.authService.register({
      body: this.registerRequest
    }).subscribe({
      next: () => {
        this.router.navigate(['activate-account']);
      },
      error: err => {
        this.errorMsg = err.error.validationErrors;
      }
    })

  }

}
