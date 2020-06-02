import { app, ipcMain, BrowserWindow } from 'electron';
import MainWindow from './mainwindow';

MainWindow.main(app, ipcMain, BrowserWindow);