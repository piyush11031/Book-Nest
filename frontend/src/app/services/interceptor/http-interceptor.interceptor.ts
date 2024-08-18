import {
  HttpEvent,
  HttpHandler,
  HttpHeaders,
  HttpInterceptor,
  HttpInterceptorFn,
  HttpRequest
} from '@angular/common/http';
import {Injectable} from "@angular/core";
import {Observable} from 'rxjs';
import {TokenService} from "../token/token.service";


@Injectable()
export class httpInterceptor implements HttpInterceptor {
  constructor(
    private tokenService: TokenService
  ) {
  }
    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.tokenService.token;

    if(token){
      //we cannot change the request, but we can clone it and change its parameters
      const authReq = request.clone({
        headers: new HttpHeaders({
          Authorization: 'Bearer ' + token
        })
      });
      return next.handle(authReq);
    }else
    {
      return next.handle(request);
    }
    }

}
