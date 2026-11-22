import { Routes } from '@angular/router';
import { MainLayoutComponent } from './shared/layouts/main-layout/main-layout.component';
import { HomeComponent } from './features/home/home.component';
import { LoginComponent } from './features/login/login.component';
import { PanelPersonalComponent } from './features/personal/panel-personal/panel-personal.component';
import { PanelAdminComponent } from './features/admin/panel-admin/panel-admin.component';
import { invitadoGuard, rolGuard } from './core/guards/rol.guard';
import { CuentaComponent } from './features/cuenta/cuenta.component';
import { ClavesComponent } from './features/admin/claves/claves.component';
import { CatalogosComponent } from './features/admin/catalogos/catalogos.component';
import { ReportarPerdidaComponent } from './features/estudiante/reportar-perdida/reportar-perdida.component';
import { MisReportesComponent } from './features/estudiante/mis-reportes/mis-reportes.component';
import { ReportarHallazgoComponent } from './features/estudiante/reportar-hallazgo/reportar-hallazgo.component';
import { EntregadosComponent } from './features/estudiante/entregados/entregados.component';
import { MisReclamosComponent } from './features/estudiante/mis-reclamos/mis-reclamos.component';
import { RevisionHallazgosComponent } from './features/personal/revision-hallazgos/revision-hallazgos.component';
import { RecepcionComponent } from './features/personal/recepcion/recepcion.component';
import { EntregasComponent } from './features/personal/entregas/entregas.component';
import { CoincidenciasComponent } from './features/personal/coincidencias/coincidencias.component';
import { ReclamosComponent } from './features/personal/reclamos/reclamos.component';
import { IncidenciasComponent } from './features/personal/incidencias/incidencias.component';
import { EstadisticasComponent } from './features/admin/estadisticas/estadisticas.component';

export const routes: Routes = [
    {
        path: 'login', component: LoginComponent, canActivate: [invitadoGuard]
    },
    {
        path: '',
        component: MainLayoutComponent,
        canActivate: [rolGuard],
        children: [
            { path: '', redirectTo: 'home', pathMatch: 'full' },

            // ===== TODOS =====
            { path: 'home', component: HomeComponent },
            { path: 'mi-cuenta', component: CuentaComponent },

            // ===== ESTUDIANTE =====
            { path: 'reportar-perdida', component: ReportarPerdidaComponent, canActivate: [rolGuard], data: { roles: ['ESTUDIANTE'] } },
            { path: 'mis-reportes', component: MisReportesComponent, canActivate: [rolGuard], data: { roles: ['ESTUDIANTE'] } },
            { path: 'reportar-hallazgo', component: ReportarHallazgoComponent, canActivate: [rolGuard], data: { roles: ['ESTUDIANTE'] } },
            { path: 'entregados', component: EntregadosComponent, canActivate: [rolGuard], data: { roles: ['ESTUDIANTE'] } },
            { path: 'mis-reclamos', component: MisReclamosComponent, canActivate: [rolGuard], data: { roles: ['ESTUDIANTE'] } },

            // ===== PERSONAL (y ADMIN) =====
            { path: 'personal', component: PanelPersonalComponent, canActivate: [rolGuard], data: { roles: ['PERSONAL', 'ADMIN'] } },
            { path: 'hallazgos', component: RevisionHallazgosComponent, canActivate: [rolGuard], data: { roles: ['PERSONAL', 'ADMIN'] } },
            { path: 'custodias', component: RecepcionComponent, canActivate: [rolGuard], data: { roles: ['PERSONAL', 'ADMIN'] } },
            { path: 'entregas', component: EntregasComponent, canActivate: [rolGuard], data: { roles: ['PERSONAL', 'ADMIN'] } },
            { path: 'coincidencias', component: CoincidenciasComponent, canActivate: [rolGuard], data: { roles: ['PERSONAL', 'ADMIN'] } },
            { path: 'reclamos', component: ReclamosComponent, canActivate: [rolGuard], data: { roles: ['PERSONAL', 'ADMIN'] } },
            { path: 'incidencias', component: IncidenciasComponent, canActivate: [rolGuard], data: { roles: ['PERSONAL', 'ADMIN'] } },

            // ===== ADMIN =====
            { path: 'admin', component: PanelAdminComponent, canActivate: [rolGuard], data: { roles: ['ADMIN'] } },
            { path: 'claves', component: ClavesComponent, canActivate: [rolGuard], data: { roles: ['ADMIN'] } },
            { path: 'catalogos', component: CatalogosComponent, canActivate: [rolGuard], data: { roles: ['ADMIN'] } },
            { path: 'estadisticas', component: EstadisticasComponent, canActivate: [rolGuard], data: { roles: ['ADMIN'] } },
        ]
    },
    {
        path: '**', redirectTo: 'login'
    }
];