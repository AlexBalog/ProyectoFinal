   M S B A M L   `   `   `  ����H  DProyectoFinal, Version=1.0.0.0, Culture=neutral, PublicKeyToken=null9!clr-namespace:ProyectoFinal.ViewsProyectoFinal.Views  5   	   C&clr-namespace:ProyectoFinal.ViewModelsProyectoFinal.ViewModels  5   	   ,    &ProyectoFinal.Views.ForgotPasswordView   5   	   W SPresentationCore, Version=8.0.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35\ XPresentationFramework, Version=8.0.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35e aSystem.Windows.Controls.Ribbon, Version=8.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089R NWindowsBase, Version=8.0.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35F 9http://schemas.microsoft.com/winfx/2006/xaml/presentation     5   	   R NSystem.Xaml, Version=8.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089:x,http://schemas.microsoft.com/winfx/2006/xaml     5   	   @d2http://schemas.microsoft.com/expression/blend/2008     5   	   Jmc;http://schemas.openxmlformats.org/markup-compatibility/2006     5   	   3local!clr-namespace:ProyectoFinal.Views     5   	   =
viewmodels&clr-namespace:ProyectoFinal.ViewModels     5   	     � Title$  Recuperar Contraseña��5	   	   $	��400��5
   	   $	��400��6    � WindowStyle$
 None=�5   	    � 
ResizeMode$ NoResize=�5   	    � WindowStartupLocation$ CenterOwner=�5   	   
������  � AllowsTransparency$
 True��5   	    � DataContext 5      6   0ProyectoFinal.ViewModels.ForgotPasswordViewModel  5   
    � 	Resources 5         	BoolToVis  ButtonAnimation �� TargetProperty W� From		 W� To
 g� Duration W� EasingFunction-  'System.Windows.Media.Animation.BackEase7  1System.Windows.Media.Animation.EasingFunctionBase   
EasingMode   	Amplitude  PrimaryButtonStyle �� 
TargetType �� Property �� Value3  -System.Windows.Media.Effects.DropShadowEffect   
BlurRadius   	Direction   ShadowDepth   Opacity   Color �� ScaleX �� ScaleY �� 
TargetType �� CornerRadius �� Padding �� Triggers P� Property P� Value  ModernTextBox ��Name
 � Tag  ��Name! �� Triggers" �� 
TargetName  WindowButtonStyle  CloseButtonStyle# �� BasedOn��%�  &	        &	      &	 �    &	 f    &	 �    &	 �    7  �� 5   
   6'   �� 5   
   .��5      W� $ Opacity�5       05      	 16   $
 0:0:0.5D�6!   W� 5      $- 'RenderTransform.(ScaleTransform.ScaleX)�5       0.85      	 16   $
 0:0:0.5D�6#    5         5!      $ EaseOut=�6   $	 0.3Q�64   5#      W� 5$      $- 'RenderTransform.(ScaleTransform.ScaleY)�5%       0.85&      	 16   $
 0:0:0.5D�6#    5'        5(      $ EaseOut=�6   $	 0.3Q�64   5*      5+      �� 5.   
   " ��6+   .�5/      ��  � �� �#3F51B56+   �� 50       � ��
 �����6+   �� 51       � ��$ SemiBold"�6+   �� 52       � ��$ 45��6'   �� 53       � ��$	 200��6&   �� 54       �@�Cursor$
 Hand��6'   �� 55       �@J�Effect 56        57      $ 15Q�6'   $	 270Q�67   $ 2Q�6G   $	 0.5Q�6W   $ #3F51B5��6e   59      �� 5:       �@J�RenderTransformOrigin$ 0.5,0.5&�66   �� 5;       � z� 5<      �� 5=      $ 1Q�6%   $ 1Q�60   5?      �� 5@       � �� 5A      �� 5B      " ��6&   .R�5C      �� #��z��6!   $
 22.5��5D   !   $
 15,5q�5E   !   ._�5F      �� $��Center=�6/   $��Center=�6L   5G      5H      5J       5K      P� 5L       �@J�IsMouseOver$
 True��61   . �5M      ��  � �� �#303F9F63   �� 5N       � z� 5O      �� 5P      $
 1.05Q�6-   $
 1.05Q�6;   5R      5S      P� 5T       � ��$
 True��6/   . �5U      ��  � �� �#1A237E63   �� 5V       � z� 5W      �� 5X      $
 0.95Q�6-   $
 0.95Q�6;   5Z      5[      P� 5\       � {�$ False��6/   . �5]      ��  � �� �#9FA8DA63   �� 5^       �@J�Effect 5_        5`      $ 10Q�6/   $	 270Q�6?   $ 1Q�6O   $	 0.3Q�6_   $ #9FA8DA��6m   5b      5c      5e      �� 5h   
   " ��6&   .�5i      ��  � ��$ 45��6'   �� 5j       � �� �#2121216+   �� 5k       � �� �#F5F5F56+   �� 5l       �@��
CaretBrush �#3F51B56+   �� 5m       � ��$ 1q�60   �� 5n       � �� �#E0E0E06,   �� 5o       � ��$ 10,0,0,0q�6(   �� 5p       � ��$ 14(�6)   �� 5q       � ��$ Center=�69   �� 5r       � �� 5s      �� 5t      " ��6&   .R�5u      �� #��z��6!   #��z��5v   !   #��z��5w   !   $
 22.5��5x   !   ._�5y      � .+�5z   "   �� $ Placeholder��6,   #��z 5{   ,   ���#BDBDBD$y�	Collapsed=�5}   ,   $��Center=�5~   ,   $��10,0,0,0q�5   ,   �� 5�   "   $  PART_ContentHost��6/   #��z��5�   /   $��Center=�5�   /   5�      5�      ! 5�      P� 5�       � ��$  ��66   . �5�   "   �� $" Placeholder��6?    � y�$ Visible=�6X   5�      P� 5�       �@J�	IsFocused$
 True��6;   . �5�   "   ��  � �� �#3F51B56@   �� 5�   "    � ��$ 2q�6D   5�      5�      5�      5�      �� 5�   
   " ��6*   .�5�      ��  � ��$ 30��6&   �� 5�       � ��$ 30��6'   �� 5�       � ��
 ���� 6+   �� 5�       � �� �#7575756+   �� 5�       � ��$ 14(�6)   �� 5�       � �� 5�      �� 5�      " ��6&   .R�5�      �� #��z��6!   $ 15��5�   !   ._�5�      �� $��Center=�6/   $��Center=�6L   5�      5�      5�       5�      P� 5�       �@J�IsMouseOver$
 True��61   . �5�      ��  � �� �#E0E0E063   �� 5�       �@�Cursor$
 Hand��6/   5�      5�      �� 5�   
   " ��6)   8#    5�      P� 5�       �@J�IsMouseOver$
 True��61   . �5�      ��  � �� �#F4433663   �� 5�       � ��
 �����63   5�      5�      5�      .��5�      �� $ 15��6   
�������._�5�   
   � $ � RowDefinitions$ 5�      �� 5�      $��30 �6    �� 5�      $��* �6    .+�5�      �� -   5        $��0��5�      ���#F5F5F5$ 	15,15,0,0��6^   ._�5�      � .+�5�      �� $��Recuperar Contraseña��6    ���#3F51B5$��Medium"�6Q   $��Center=�6e   $��10,0,0,0q�6�   �� 5�      % �� Orientation$% 
Horizontal=�6!   $��Right=�6:   .+�5�      �� -   5        & ��Name$& btnClose��5�   !   #��[ 6J   $	��✕��6t   $
��Bold"�6�   5�      5�      5�      � 5�      $��1��6   $��30,20q�6    $ 5�      �� 5�      $
��Auto �6$   �� 5�      $
��Auto �6$   �� 5�      $
��Auto �6$   �� 5�      $
��Auto �6$   �� 5�      $��* �6$   �� 5�      $
��Auto �6$   .+�5�      (� $��60��6   $��60��6%   $��0��61   $��0,0,0,15q�6>   .��5�      �� $��24��6   $��24��6(   .+�5�      F� ���#3F51B5����� �  	= � $�  z � 	=  	= � �  � � 	=  	= � z  z � 	=  	= � �  	= � 	=  	= � � ��� ��Q��k0@���  -1���  -1��� -1 -1� 	=  -1� 	= ��� 	= ��� `�s ���  � ��� 	6.   5�      5�      �� 5�      $��1��6   $_��YIngresa tu correo electrónico y te enviaremos un enlace para restablecer tu contraseña.��6)   $
��Wrap=�5�      ' �� TextAlignment$' Center=�60   ���#757575$��14(�5�   1   $��0,0,0,20q�6?   �� 5�      $��2��6   .+�5�      �� $��Correo electrónico��6    ���#757575$��13(�6O   $��5,0,0,5q�6]   �� 5�      #��[ 6   $ Correo electrónico��5�      $��0,0,0,10q�5�      ��5�      �� *Email+( �� UpdateSourceTrigger$( PropertyChanged=�5�      �� 5�      ���#F44336$��12(�5�   5   $��5,0,0,10q�5�       ��5�       �� *ErrorMessage+y�5�       �� *
HasError+) �� 	Converter#) [  5�       �� 5�      ���#E8F5E9���#C8E6C9$��1q�6H   $ 5��5�      $ 10q�6.   $��0,0,0,15q�6;   y�5�      �� *
HasSuccess+#) [  ._�5�      �� ���#4CAF50$
��Wrap=�5�   9   ��5�   $   �� *SuccessMessage+5�   9   5�      5�      � 5�      $��3��6   $��0,10,0,0q�6$   .+�5�      �� $��Enviar Instrucciones��6   #��[ 5      $��Center=�5     * J� RenderTransformOrigin$* 0.5,0.5&�5     ��5     �� *ResetPasswordCommand+{�5     �� *IsNotProcessing+z�5     �� 5     + � Triggers+ 5     :� 5	     , :� RoutedEvent$, Button.Loaded��6+   .[�5
  "   �� #��[ 62   5     5     � 5     $	��200��6"   $��5��5      - � IsIndeterminate$
- True��5      ���#3F51B5���#E0E0E0$��0,55,0,0q�5      y�5      �� *IsProcessing+#) [  5      5     �� 5     -   5        $��5��5     $��Cancelar��5     
������ ���#757575$��0q�5     $��0q�5      $��Center=�5!     $��Bottom=�5"     . � Cursor$
. Hand��5#     $��0,0,0,5q�5$     ��5%     �� 5&     " ��6    .�5'     ��  � �� 5(  "   �� 5)  &   " ��66   .R�5*  *   �� 5+  '   5-      5.     P� 5/  "    �@J�IsMouseOver$
 True��6A   . �50  &   ��  � �� �#3F51B56C   51  #   53     55     56     57     58     59     