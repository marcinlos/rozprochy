 module MiddlewareTestbed {
 
   interface Item;
   
   exception ItemError { };
   
   exception ItemNotExists extends ItemError { };
   
   exception ItemAlreadyExists extends ItemError { };
   
   exception ItemBusy extends ItemError {};
   
   interface AFactory {
      Item* createItem(string name, string type) throws ItemAlreadyExists;
      Item* takeItem(string name) throws ItemNotExists, ItemBusy;
      idempotent void releaseItem(string name) throws ItemNotExists;
   };
   
   interface Item {
      idempotent string name();
      idempotent int getItemAge();
   };
   
   interface ItemA extends Item {
      void actionA(float a, out long b);
   };
   
   interface ItemB extends Item {
      float actionB(string a);
   };
   
   interface ItemC extends Item {
      void actionC(int a, out int aOut, out short b);
   };
}; 