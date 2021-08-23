import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { NavigationService, NavType } from '../navigation.service';
import { NotificationService } from '../notification.service';
import { UserService } from '../user.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
})
export class LoginComponent implements OnInit {
  usernameInput: string = '';
  passwordInput: string = '';

  constructor(
    private userService: UserService,
    private nav: NavigationService,
    private notifi: NotificationService
  ) {}

  ngOnInit() {}

  /**
   * login request
   */
  public login(): void {
    if (!this.usernameInput || !this.passwordInput) {
      this.notifi.toast('Please enter username and password');
      return;
    }
    this.userService.login(this.usernameInput, this.passwordInput).subscribe({
      next: (resp) => {
        // login successful
        this.userService.fetchUserInfo();
        this.nav.navigateTo(NavType.ROOM_LIST);
      },
      complete: () => {
        this.passwordInput = '';
      },
    });
  }

  passwordInputKeyPressed(event: any): void {
    if (event.key === 'Enter') {
      this.login();
    }
  }
}
