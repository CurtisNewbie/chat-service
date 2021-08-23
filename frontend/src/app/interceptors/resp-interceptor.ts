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
import { Observable } from 'rxjs';
import { catchError, filter } from 'rxjs/operators';
import { Router } from '@angular/router';
import { NotificationService } from '../notification.service';
import { Resp } from '../models/resp';

/**
 * Intercept http response with 'Resp' as body
 */
@Injectable()
export class RespInterceptor implements HttpInterceptor {
  constructor(private router: Router, private notifi: NotificationService) {}

  intercept(
    httpRequest: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    return next.handle(httpRequest).pipe(
      filter((e, i) => {
        if (!(e instanceof HttpResponse || e instanceof HttpHeaderResponse)) {
          return true;
        }
        console.log('Intercept HttpResponse:', e);
        if (e instanceof HttpResponse) {
          // normal http response with body, check if it has any error by field 'hasError'
          let r: Resp<any> = e.body as Resp<any>;
          if (r.hasError) {
            this.notifi.toast(r.msg, -1);
            // filter out this value
            return false;
          }
        }
        return true;
      })
    );
  }
}
