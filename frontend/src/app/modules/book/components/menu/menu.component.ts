import {Component, OnInit} from '@angular/core';

@Component({
  selector: 'app-menu',
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.scss'
})
export class MenuComponent implements OnInit{
    ngOnInit(): void {
      //selects all elements having .nav-link class
      const linkColor = document.querySelectorAll('.nav-link');


      linkColor.forEach(link => {

        //sets the link active if it is open in the browser window
        if(window.location.href.endsWith(link.getAttribute('href') || '')){
          link.classList.add('active'); //add the class "active" defined in menu.comp.scss to the active link
        }

        //if a link gets clicked, then make the current active link inactive & make this link active
        link.addEventListener('click', () => {
          linkColor.forEach(l => l.classList.remove('active'));
          link.classList.add('active');
        });
      });
    }

  logout() {
      localStorage.removeItem('token');
      window.location.reload();
  }

}
