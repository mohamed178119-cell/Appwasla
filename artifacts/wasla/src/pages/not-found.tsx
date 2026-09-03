import { Card, CardContent } from "@/components/ui/card";
import { AlertCircle, ArrowRight } from "lucide-react";
import { useLocation } from "wouter";

export default function NotFound() {
  const [, setLocation] = useLocation();
  return (
    <div className="wasla-shell flex min-h-[100dvh] w-full items-center justify-center px-5 text-right" dir="rtl">
      <Card className="w-full max-w-md border-[#2b3e58] bg-[#14243a] text-[#e8edf7]">
        <CardContent className="p-7">
          <div className="mb-5 flex items-center gap-3">
            <AlertCircle className="h-8 w-8 text-[#b8a5ff]" />
            <h1 className="text-2xl font-bold">
              هذه الوصلة غير موجودة
            </h1>
          </div>
          <p className="mt-4 text-sm leading-7 text-[#8ea0b7]">
            يبدو أن العنوان الذي فتحته ليس جزءاً من مساحة وصلة.
          </p>
          <button data-testid="button-back-home" onClick={() => setLocation("/")} className="mt-6 flex h-11 items-center gap-2 rounded-xl bg-[#52e2c7] px-4 text-sm font-bold text-[#0e1727]">
            العودة إلى وصلة <ArrowRight className="h-4 w-4" />
          </button>
        </CardContent>
      </Card>
    </div>
  );
}
