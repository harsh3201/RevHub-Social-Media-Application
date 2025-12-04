import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [RouterModule, CommonModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {
  displayText = '';
  fullText = 'RevHub';
  showForm = false;
  
  ngOnInit() {
    this.typeText();
  }
  
  typeText() {
    let i = 0;
    const interval = setInterval(() => {
      this.displayText = this.fullText.substring(0, i + 1);
      i++;
      if (i >= this.fullText.length) {
        clearInterval(interval);
        setTimeout(() => {
          this.showForm = true;
        }, 500);
      }
    }, 300);
  }
}
