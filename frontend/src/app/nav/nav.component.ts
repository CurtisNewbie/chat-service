import { Component, OnInit } from '@angular/core';
import { UserService } from '../user.service';

@Component({
  selector: 'app-nav',
  templateUrl: './nav.component.html',
  styleUrls: ['./nav.component.css'],
})
export class NavComponent implements OnInit {
  isLoggedIn: boolean = false;

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    if (!this.userService.hasUserInfo()) {
      this.userService.fetchUserInfo();
    }
    this.userService.isLoggedInObservable.subscribe({
      next: (isLoggedIn) => {
        this.isLoggedIn = isLoggedIn;
      },
    });
  }

  /** log out current user and navigate back to login page */
  logout(): void {
    this.userService.logout().subscribe({
      complete: () => {
        console.log('Logged out user, navigate back to login page');
      },
    });
  }
}
