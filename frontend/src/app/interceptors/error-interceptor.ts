import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpEvent,
  HttpResponse,
  HttpRequest,
  HttpHandler,
  HttpErrorResponse,
  HttpHeaderResponse,
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, filter } from 'rxjs/operators';
import { Router } from '@angular/router';
import { UserService } from '../user.service';
import { NotificationService } from '../notification.service';

/**
 * Intercept http error response
 */
@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(
    private router: Router,
    private userService: UserService,
    private notifi: NotificationService
  ) {}

  intercept(
    httpRequest: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    return next.handle(httpRequest).pipe(
      catchError((e) => {
        if (e instanceof HttpErrorResponse) {
          console.log('Http error response status:', e.status);
          // status code 5xx or 0, server is down or unable to connect
          if (e.status >= 500 || e.status == 0) {
            this.notifi.toast('Server is down');
            this.setLogout();
          } else if (e.status === 401 || e.status === 403) {
            // status code 401 or 403, redirect to login page
            this.notifi.toast('Please login first');
            this.setLogout();
          } else if (e.status >= 300 || e.status < 400) {
            // status code 3xx, redirect to login page
            this.notifi.toast('Please login first');
            this.setLogout();
          } else {
            // other status code
            this.notifi.toast('Unknown server error');
          }
          return throwError(e);
        }
      })
    );
  }

  private setLogout(): void {
    this.userService.setLogout();
  }
}
