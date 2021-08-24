import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { HEADERS } from './models/Headers';
import { Resp } from './models/resp';
import { ChangePasswordParam, UserInfo } from './models/user-info';
import { NavigationService, NavType } from './navigation.service';
import { NotificationService } from './notification.service';
import { buildApiPath } from './util/api-util';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private userInfo: UserInfo = null;
  private roleSubject = new Subject<string>();
  private usernameSubject = new Subject<string>();
  private isLoggedInSubject = new Subject<boolean>();

  roleObservable: Observable<string> = this.roleSubject.asObservable();
  isLoggedInObservable: Observable<boolean> =
    this.isLoggedInSubject.asObservable();
  usernameObservable: Observable<string> = this.usernameSubject.asObservable();

  constructor(
    private http: HttpClient,
    private nav: NavigationService,
    private notifi: NotificationService
  ) {}

  /**
   * Attempt to signin
   * @param username
   * @param password
   */
  public login(username: string, password: string): Observable<Resp<void>> {
    let formData = new FormData();
    formData.append('username', username);
    formData.append('password', password);
    return this.http.post<Resp<void>>(buildApiPath('/login'), formData, {
      withCredentials: true,
    });
  }

  /**
   * Logout current user
   */
  public logout(): Observable<void> {
    this.setLogout();
    return this.http.get<void>(buildApiPath('/logout'), {
      withCredentials: true,
    });
  }

  /**
   * Set user being logged out
   */
  public setLogout(): void {
    this.userInfo = null;
    this.notifyLoginStatus(false);
    this.nav.navigateTo(NavType.LOGIN_PAGE);
  }

  /**
   * Fetch user info
   */
  public fetchUserInfo(): void {
    this.http
      .get<Resp<UserInfo>>(buildApiPath('/user/info'), {
        withCredentials: true,
      })
      .subscribe({
        next: (resp) => {
          if (resp.data != null) {
            this.userInfo = resp.data;
            this.notifyRole(this.userInfo.role);
            this.notifyLoginStatus(true);
            this.usernameSubject.next(this.userInfo.username);
          } else {
            this.notifi.toast('Please login first');
            this.nav.navigateTo(NavType.LOGIN_PAGE);
            this.notifyLoginStatus(false);
          }
        },
      });
  }

  /** Notify the role of the user via observable */
  private notifyRole(role: string): void {
    this.roleSubject.next(role);
  }

  /** Notify the login status of the user via observable */
  private notifyLoginStatus(isLoggedIn: boolean): void {
    this.isLoggedInSubject.next(isLoggedIn);
  }

  /**
   * Get user info that is previously fetched
   */
  public getUserInfo(): UserInfo {
    return this.userInfo;
  }

  /**
   * Check if the service has the user info already
   */
  public hasUserInfo(): boolean {
    return this.userInfo != null;
  }

  /**
   * Change password
   */
  public changePassword(param: ChangePasswordParam): Observable<Resp<any>> {
    return this.http.post<Resp<any>>(
      buildApiPath('/user/password/update'),
      param,
      HEADERS
    );
  }
}
