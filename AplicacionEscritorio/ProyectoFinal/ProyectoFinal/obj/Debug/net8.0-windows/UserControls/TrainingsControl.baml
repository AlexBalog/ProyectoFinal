   M S B A M L   `   `   `  ����H  DProyectoFinal, Version=1.0.0.0, Culture=neutral, PublicKeyToken=nullG(clr-namespace:ProyectoFinal.UserControlsProyectoFinal.UserControls  5      C&clr-namespace:ProyectoFinal.ViewModelsProyectoFinal.ViewModels  5      1    +ProyectoFinal.UserControls.TrainingsControl   5      W SPresentationCore, Version=8.0.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35\ XPresentationFramework, Version=8.0.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35e aSystem.Windows.Controls.Ribbon, Version=8.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089R NWindowsBase, Version=8.0.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35F 9http://schemas.microsoft.com/winfx/2006/xaml/presentation     5      R NSystem.Xaml, Version=8.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089:x,http://schemas.microsoft.com/winfx/2006/xaml     5      @d2http://schemas.microsoft.com/expression/blend/2008     5      Jmc;http://schemas.openxmlformats.org/markup-compatibility/2006     5      :local(clr-namespace:ProyectoFinal.UserControls     5      =
viewmodels&clr-namespace:ProyectoFinal.ViewModels     5        � DataContext  5      1   +ProyectoFinal.ViewModels.TrainingsViewModel  5   
    � 	Resources 5         PrimaryColor  BackgroundColor  SurfaceColor  TextColorPrimary  TextColorSecondary  AccentColor  PrimaryBrush �� Color  BackgroundBrush  SurfaceBrush 	 TextBrushPrimary 
 TextBrushSecondary  AccentBrush  	BoolToVis  FilterTextBox �� 
TargetType �� Property �� Value �� 
TargetType �� CornerRadius ��Name	 �� Triggers
 P� Property P� Value  FilterComboBox d�Name �� 	ClickMode d� 	IsChecked �� Mode �� RelativeSource � ColumnDefinitions ��Name J� IsHitTestVisible �� SelectionBoxItem �� SelectionBoxItemTemplate ��Name �� 
IsReadOnly �� 
IsReadOnly �Name �� IsDropDownOpen � AllowsTransparency � ActualWidth K� IsItemsHost �� Triggers �� 
TargetName  ComboBoxItemStyle  �� Padding  ActionButton  
EditButton! �� BasedOn  DeleteButton  ApproveButton  RejectButton  	AddButton  DataGridColumnHeaderStyle=  7System.Windows.Controls.Primitives.DataGridColumnHeader  ModernDataGrid&   System.Windows.Controls.DataGrid��%A  &	        &	      &	 >     &	 ]     &	 |     &	 �     &	 �     7   &	 �     7  &	 �     7  &		     7  &	
     7  &	 2    7  &	 J    &	 ]    7  7 	 7  7  &	 �    7  7 	 7  7 
 7  7  7  &	 �    7 	 7  7  &	 �    &	 _    7  &	 M    7  &	 ;    7  &	 )    7  &	     7  &	 �    7  7 	 7  &	 �    7  7 	 7  7  7  7  ��5   
   #8A2BE2��6.   ��5   
   #121212��61   ��5   
   #1E1E1E��6.   ��5   
   #FFFFFF��62   ��5   
   #E0E0E0��64   ��5   
   #333333��6-   �� 5   
   8   6/   �� 5   
   8   62   �� 5   
   8   6/   �� 5   
   8   63   �� 5   
   8   65   �� 5   
   8   6.   �� 5    
   6'   �� 5#   
   " ��6&   .�5$      ��  � ��$ 35��6'   �� 5%       � ��$
 10,0q�6(   �� 5&       � ��$ Center=�69   �� 5'       � ��8   6+   �� 5(       � ��8  6+   �� 5)       � ��8  6,   �� 5*       � ��$ 1q�60   �� 5+       � �� 5,      �� 5-      " ��6&   .R�5.      �� #��z��6!   #��z��5/   !   #��z��50   !   $ 5��51   !   ._�52      �� $ PART_ContentHost��6+   #��z��53   +   54      55      57      	 58      P� 59      
 �@J�	IsFocused$
 True��6/   . �5:      ��  � ��8  64   �� 5;       � ��$ 2q�68   5<      5>      �� 5A   
   " ��6'   .�5B      ��  � ��$ 35��6'   �� 5C       � ��$
 10,0q�6(   �� 5D       � ��8   6+   �� 5E       � ��8  6+   �� 5F       � ��8  6,   �� 5G       � ��$ 1q�60   �� 5H       � �� 5I      �� 5J      " ��6&   .R�5K      � .+�5L      d� $ ToggleButton��6+   #��z��5M   #   #��z��5N   #   #��z��5O   #   $|�False��5P   #   $ Press=�5R   #    5Q   #   �� *IsDropDownOpen+$ TwoWay=� �� *TemplatedParent+��5S   "   �� 5T   &   " d�66   .R�5U   *   �� #��z��61   #��z��5V   )   #��z��5W   )   $ 5��5X   )   ._�5Y   .   �  5Z   2   �� 5[   6   $��* �6G   �� 5\   6   $��20 �6G   .+�5^   2   F� $��1��67   $��Center=�5_   /   $��Center=�5`   /   "����        � 	=  	= � z     	8�� 5b   /   5c   /   5d   +   5e   '   5g      �� 5i      $ ContentSite��6/   $ False��5j   '   #��z 5k   '   #��z 5l   '   #��z��5m   '   $��	10,0,25,0q�5n   '   $��Center=�5o   '   $
��Left=�5p   '   �� 5r      $ PART_EditableTextBox��6&   $
��Left=�5t   &   $��Center=�5u   &   $��	10,0,25,0q�5v   &   $
|�True��5w   &   
������ $��0q�5y   &   #��z��5z   &   $y�Hidden=�5{   &   # z 5|   &   ��5s   &   Z� 5|   &   � 5~      $ Popup��6$   $��Bottom=�5      #��z 5�      $
 True��5�      $|�False��5�      $��Slide=�5�      .��5�   "   � $	��200��6'   #��z 67   .+�5�   &   �� 8�� 8�� $��1q�5�   %   $ 5��5�   %   ._�5�   *   �� .��5�   .   �� $
 True��69   5�   +   5�   '   5�   #   5�      5�       5�      P� 5�      
 �@��HasItems$ false��6:   . �5�   "   �� $ PART_EditableTextBox��6)    � y�$ Visible=�6a   5�      P� 5�      
 �@��
IsGrouping$
 true��6<   . �5�   "   ��  � ��$ false��6R   5�      5�      5�      	 5�      P� 5�      
 �@J�	IsFocused$
 True��6/   . �5�      ��  � ��8  64   �� 5�       � ��$ 2q�68   5�      5�      �� 5�   
   " ��6*   .�5�      ��  � ��
 ���� 6+   �� 5�       � ��8   6+   �� 5�       � ��$
 10,5q�6(   �� 5�       � �� 5�      �� 5�      " ��6&   .R�5�      �� #��z��6!   #  z��5�      ._�5�      �� 5�      5�      5�      	 5�      P� 5�      
 �@J�IsMouseOver$
 True��61   . �5�      ��  � ��8  63   5�      P� 5�      
 �@��
IsSelected$
 True��60   . �5�      ��  � ��8  63   5�      5�      �� 5�   
   " ��6%   .�5�      ��  � ��$ 30��6&   �� 5�       � ��$ 30��6'   �� 5�       � ��$ 2q�6'   �� 5�       � ��$ 0q�60   �� 5�       �@�Cursor$
 Hand��6'   �� 5�       � �� 5�      �� 5�      " ��6&   .R�5�      �� #��z��6!   $ 5��5�   !   ._�5�      �� $��Center=�6/   $��Center=�5�   /   5�      5�      5�      5�      �� 5�   
   " ��6#   8!   .�5�      ��  � �� �#2196F36+   �� 5�       � ��
 �����6+   	 5�      P� 5�      
 �@J�IsMouseOver$
 True��61   . �5�      ��  � �� �#1976D263   5�      5�      �� 5�   
   " ��6%   8!   .�5�      ��  � �� �#F443366+   �� 5�       � ��
 �����6+   	 5�      P� 5�      
 �@J�IsMouseOver$
 True��61   . �5�      ��  � �� �#D32F2F63   5�      5�      �� 5�   
   " ��6&   8!   .�5�      ��  � �� �#4CAF506+   �� 5�       � ��
 �����6+   	 5�      P� 5�      
 �@J�IsMouseOver$
 True��61   . �5�      ��  � �� �#388E3C63   5�      5�      �� 5�   
   " ��6%   8!   .�5�      ��  � �� �#FF98006+   �� 5�       � ��
 �����6+   	 5�      P� 5�      
 �@J�IsMouseOver$
 True��61   . �5�      ��  � �� �#F57C0063   5�      5�      �� 5�   
   " ��6"   .�5�      ��  � ��8   6+   �� 5�       � ��
 �����6+   �� 5�       � ��$ 40��6'   �� 5�       � ��$
 15,0q�6(   �� 5�       � ��$ 0q�60   �� 5�       �@�Cursor$
 Hand��6'   �� 5�       � ��$ SemiBold"�6+   �� 5       � �� 5     �� 5     " ��6&   .R�5     �� #��z��6!   $ 5��5  !   #  z��5  !   ._�5     �� $��Center=�6/   $��Center=�5  /   5     5	     5     	 5     P� 5     
 �@J�IsMouseOver$
 True��61   . �5     ��  � �� �#9C27B063   5     5     �� 5  
   "  62   .�5     ��  � ��8   6+   �� 5      � ��8  6+   �� 5      � ��$ SemiBold"�6+   �� 5      � ��$
 10,8q�6(   �� 5      � ��8  6,   �� 5      � ��$ 0,0,1,1q�60   5     �� 5  
   "  6'   .�5     ��  � ��8   6+   �� 5       � ��8  6+   �� 5!      � ��8  6,   �� 5"      � ��$ 1q�60   �� 5#      �@ RowBackground8  6.   �� 5$       �@ AlternatingRowBackground �#25252569   �� 5%      �@ GridLinesVisibility$ 
Horizontal=�64   �� 5&       �@ HorizontalGridLinesBrush8  69   �� 5'      �@ HeadersVisibility$ Column=�62   �� 5(      �@ AutoGenerateColumns$ False��64   �� 5)      �@ CanUserAddRows$ False��6/   �� 5*      �@ CanUserDeleteRows$ False��62   �� 5+      �@ 
IsReadOnly$
 True��6+   �� 5,      �@ SelectionMode$ Single=�6.   �� 5-      �@ SelectionUnit$ FullRow=�6.   �� 5.      �@ ColumnHeaderStyle8  62   5/     50     .��52     � " � RowDefinitions" 53  
   �� 54     $
��Auto �6   �� 56     $
��Auto �6   �� 58     $��* �6   �� 5:     $
��Auto �6   .+�5?  
   �� $��0��6   #��[ 6   $ 5��5@     $  20q�6"   $��0,0,0,20q�6/   ._�5A     �� .+�5B     �� $��🔍 Filtros de Búsqueda��6   $��16(�5C     $��SemiBold"�6*   #��[	 5D     $��0,0,0,15q�5E     � 5G      5H     �� 5I     $��* �6+   �� 5J     $��* �6+   �� 5K     $��* �6+   �� 5L     $��* �6+   �� 5M     $
��Auto �6+   " 5O     �� 5P     $
��Auto �6(   �� 5Q     $
��Auto �6(   .+�5U     �� $��0��6!   $��0,0,10,0q�61   .+�5V     �� $��Nombre��6$   #��[
 62   $��12(�5W  $   $��0,0,0,5q�62   �� 5X     #��[ 6"   ��5Y  "   �� *FilterNombre+# �� UpdateSourceTrigger$# PropertyChanged=�5Z     �� 5\     $��1��6!   $	��5,0q�61   .+�5]     �� $��
Categoría��6$   #��[
 65   $��12(�5^  $   $��0,0,0,5q�62   �� 5_     #��[ 6#   #��[ 5`  #   $ �� SelectedItem$ 5a  #   �� *FilterCategoria+@�5b     O Kmscorlib, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e0891&clr-namespace:System;assembly=mscorlibSystem 5c  0   ��6"   9system&clr-namespace:System;assembly=mscorlib     60   	Todas��6m   1&clr-namespace:System;assembly=mscorlibSystem 5d  0   ��6"   9system&clr-namespace:System;assembly=mscorlib     60   
Fuerza��6n   1&clr-namespace:System;assembly=mscorlibSystem 5e  0   ��6"   9system&clr-namespace:System;assembly=mscorlib     60   
Cardio��6n   1&clr-namespace:System;assembly=mscorlibSystem 5f  0   ��6"   9system&clr-namespace:System;assembly=mscorlib     60   Flexibilidad��6t   1&clr-namespace:System;assembly=mscorlibSystem 5g  0   ��6"   9system&clr-namespace:System;assembly=mscorlib     60   Resistencia��6s   1&clr-namespace:System;assembly=mscorlibSystem 5h  0   ��6"   9system&clr-namespace:System;assembly=mscorlib     60   Hipertrofia��6s   1&clr-namespace:System;assembly=mscorlibSystem 5i  0   ��6"   9system&clr-namespace:System;assembly=mscorlib     60   Fullbody��6p   5k     5l     �� 5n     $��2��6!   $	��5,0q�61   .+�5o     �� $��Músculo Principal��6$   #��[
 6=   $��12(�5p  $   $��0,0,0,5q�62   �� 5q     #��[ 6#   #��[ 5r  #   $ 5s  #   �� *FilterMusculoPrincipal+@�5t     1&clr-namespace:System;assembly=mscorlibSystem 5u  0   ��6"   9system&clr-namespace:System;assembly=mscorlib     60   	Todos��6m   1&clr-namespace:System;assembly=mscorlibSystem 5v  0   ��6"   9system&clr-namespace:System;assembly=mscorlib     60   	Pecho��6m   1&clr-namespace:System;assembly=mscorlibSystem 5w  0   ��6"   9system&clr-namespace:System;assembly=mscorlib     60   Espalda��6o   1&clr-namespace:System;assembly=mscorlibSystem 5x  0   ��6"   9system&clr-namespace:System;assembly=mscorlib     60   
Cuadriceps��6r   1&clr-namespace:System;assembly=mscorlibSystem 5y  0   ��6"   9system&clr-namespace:System;assembly=mscorlib     60   
Biceps��6n   1&clr-namespace:System;assembly=mscorlibSystem 5z  0   ��6"   9system&clr-namespace:System;assembly=mscorlib     60   Hombros��6o   1&clr-namespace:System;assembly=mscorlibSystem 5{  0   ��6"   9system&clr-namespace:System;assembly=mscorlib     60   Triceps��6o   1&clr-namespace:System;assembly=mscorlibSystem 5|  0   ��6"   9system&clr-namespace:System;assembly=mscorlib     60   Gluteos��6o   1&clr-namespace:System;assembly=mscorlibSystem 5}  0   ��6"   9system&clr-namespace:System;assembly=mscorlib     60   Femoral��6o   1&clr-namespace:System;assembly=mscorlibSystem 5~  0   ��6"   9system&clr-namespace:System;assembly=mscorlib     60   Gemelos��6o   1&clr-namespace:System;assembly=mscorlibSystem 5  0   ��6"   9system&clr-namespace:System;assembly=mscorlib     60   Abdominales��6s   1&clr-namespace:System;assembly=mscorlibSystem 5�  0   ��6"   9system&clr-namespace:System;assembly=mscorlib     60   Lumbares��6p   1&clr-namespace:System;assembly=mscorlibSystem 5�  0   ��6"   9system&clr-namespace:System;assembly=mscorlib     60   
Antebrazos��6r   1&clr-namespace:System;assembly=mscorlibSystem 5�  0   ��6"   9system&clr-namespace:System;assembly=mscorlib     60   	Aductores��6q   1&clr-namespace:System;assembly=mscorlibSystem 5�  0   ��6"   9system&clr-namespace:System;assembly=mscorlib     60   
Abductores��6r   5�     5�     �� 5�     $��3��6!   $��10,0,0,0q�61   .+�5�     �� $��Estado��6$   #��[
 62   $��12(�5�  $   $��0,0,0,5q�62   �� 5�     #��[ 6#   #��[ 5�  #   $ 5�  #   �� *FilterEstado+@�5�     1&clr-namespace:System;assembly=mscorlibSystem 5�  0   ��6"   9system&clr-namespace:System;assembly=mscorlib     60   	Todos��6m   1&clr-namespace:System;assembly=mscorlibSystem 5�  0   ��6"   9system&clr-namespace:System;assembly=mscorlib     60   	Aprobados��6q   1&clr-namespace:System;assembly=mscorlibSystem 5�  0   ��6"   9system&clr-namespace:System;assembly=mscorlib     60   
Pendientes��6r   1&clr-namespace:System;assembly=mscorlibSystem 5�  0   ��6"   9system&clr-namespace:System;assembly=mscorlib     60   Sin solicitar��6u   5�     5�     �� 5�     $��4��6!   $	��5,0q�61   .+�5�     �� $��Estado Baja��6$   #��[
 67   $��12(�5�  $   $��0,0,0,5q�62   �� 5�     #��[ 6#   #��[ 5�  #   $ 5�  #   �� *
FilterBaja+@�5�     1&clr-namespace:System;assembly=mscorlibSystem 5�  0   ��6"   9system&clr-namespace:System;assembly=mscorlib     60   	Todos��6m   1&clr-namespace:System;assembly=mscorlibSystem 5�  0   ��6"   9system&clr-namespace:System;assembly=mscorlib     60   Activos��6o   1&clr-namespace:System;assembly=mscorlibSystem 5�  0   ��6"   9system&clr-namespace:System;assembly=mscorlib     60   Dados de baja��6u   5�     5�     �� 5�     $��1��6!   $��0��6.   $��	0,10,10,0q�6>   .+�5�     �� $��Duración Min (min)��6$   #��[
 6>   $��12(�5�  $   $��0,0,0,5q�62   �� 5�     #��[ 6"   ��5�  "   �� *FilterDuracionMin+$# PropertyChanged=�5�     �� 5�     $��1��6!   $��1��6.   $��5,10,5,0q�6>   .+�5�     �� $��Duración Max (min)��6$   #��[
 6>   $��12(�5�  $   $��0,0,0,5q�62   �� 5�     #��[ 6"   ��5�  "   �� *FilterDuracionMax+$# PropertyChanged=�5�     �� 5�     $��1��6!   $��2��6.   $��5,10,5,0q�6>   .+�5�     �� $��Creador��6$   #��[
 63   $��12(�5�  $   $��0,0,0,5q�62   �� 5�     #��[ 6"   ��5�  "   �� *FilterCreador+$# PropertyChanged=�5�     �� 5�     $��1��6!   $��4��6.   % �� Orientation$% 
Horizontal=�6>   $��Bottom=�5�  !   $��	10,10,0,0q�6<   .+�5�     �� $��🔍 Buscar��6!   #��[ 65   $��0,0,10,0q�5�  I   ��6!   �� *ApplyFiltersCommand+6I   �� 5�     $��🗑️ Limpiar��6!   #��[ 67   ��5�  !   �� *ClearFiltersCommand+5�     5�     5�     5�     � 5�  
   $��1��6   $��0,0,0,15q�6    5�     �� 5�     $��* �6#   �� 5�     $
��Auto �6#   .+�5�     �� $��0��6   $��14(�5�     #��[
 5�     $��Center=�5�     ��5�     �� *
StatusText+5�     �� 5�     $��1��6   $��➕ Agregar Entrenamiento��5�     #��[ 5�     ��5�     �� *AddTrainingCommand+5�       5�  
   $��2��6   #��[ 5�     ��5�     �� *Entrenamientos+$ 5�     �� *SelectedTraining+&   Columns& 5�     0  *System.Windows.Controls.DataGridTextColumn  5�     ,  &System.Windows.Controls.DataGridColumn'   Header$' ID��6%   (   Width5  /System.Windows.Controls.DataGridLengthConverter$( 80 6I   1  +System.Windows.Controls.DataGridBoundColumn)   Binding) 61   �� *_id+6I     5�     $' Nombre��6%   $	( 200 6P   ) 65   �� *nombre+6P     5�     $' 
Categoría��6%   $	( 120 6V   ) 68   �� *	categoria+6V     5�     $' Músculo Principal��6%   $	( 140 6e   ) 6@   �� *musculoPrincipal+6e     5�     $' 	Duración��6%   $	( 100 6Y   ) 67   �� *DuracionTexto+6Y     5�     $' Creador��6%   $	( 150 6R   ) 66   �� *	creador+6R     5�     $' Estado��6%   $	( 120 6Z   ) 65   �� *EstadoAprobacion+6Z     5�     $' Estado Baja��6%   $	( 100 6Y   ) 6:   �� *
EstadoBaja+6Y     5�     $' Likes��6%   $( 80 6N   ) 64   �� *likes+6N   4  .System.Windows.Controls.DataGridTemplateColumn  5�     $' Acciones��6)   $	( 220 6;   *   CellTemplate* 5�     �� 5�     .R�5�     �� $% 
Horizontal=�6)   $��Center=�6B   .+�5   "   �� #��[ 6)   $��✏️��5  )   + � ToolTip$+ Editar��66   ��5  )   �� *!DataContext.EditTrainingCommand+ �� , �� AncestorType, UserControl��5  )   �� �� 5  "   #��[ 6)   $	��✅��5  )   $+ Aprobar��65   ��5  )   �� *$"DataContext.ApproveTrainingCommand+ �� , UserControl��5	  )   �� y�5
  )   �� *CanBeApproved+- �� 	Converter#- [ �� 5  "   #��[ 6)   $	��❌��5  )   $+ Rechazar��65   ��5  )   �� *#!DataContext.RejectTrainingCommand+ �� , UserControl��5  )   �� y�5  )   �� *CanBeRejected+#- [ �� 5  "   #��[ 6)   $��⬇️��5  )   $+ Dar de baja��66   ��5  )   �� *'%DataContext.DeactivateTrainingCommand+ �� , UserControl��5  )   �� y�5  )   �� *CanBeBajado+#- [ �� 5  "   #��[ 6)   $��⬆️��5  )   $+ 	Reactivar��66   ��5  )   �� *'%DataContext.ReactivateTrainingCommand+ �� , UserControl��5  )   �� y�5  )   �� *CanBeReactivated+#- [ �� 5!  "   #��[ 6)   $��🗑️��5"  )   $+ Eliminar��67   ��5#  )   �� *#!DataContext.DeleteTrainingCommand+ �� , UserControl��5$  )   �� 5%     5&     5(     5*     �� 5-  
   $��3��6   #��[ 6   $ 5��5.     $  15q�6"   $��0,15,0,0q�6/   y�5/     �� *	IsLoading+#- [ ._�50     �� $% 
Horizontal=�6   $��Center=�62   .+�51     �� $	��⏳��6   $��16(�6%   $��0,0,10,0q�63   �� 52     $ ��Cargando entrenamientos...��6   #��[
 53     54     55     56     57     